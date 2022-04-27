package be.nayima.blueprint.async.basicjob.integration;

import be.nayima.blueprint.async.AsyncBlueprintApplication;
import be.nayima.blueprint.async.basicjob.mock.MockFailingPartyCaller;
import be.nayima.blueprint.async.persistent.message.PersistentJob;
import be.nayima.blueprint.async.persistent.processor.PersistentJobProcessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.junit.RabbitAvailable;
import org.springframework.amqp.rabbit.test.context.SpringRabbitTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringRabbitTest()
@SpringBootTest(classes = {AsyncBlueprintApplication.class})
@ActiveProfiles(profiles = {"local-rabbitmq","test"})
@RabbitAvailable
public class DlqRetryIntegrationTest {
    public static final int MESSAGES = 4;
    public static final int ITERATIONS = 3;
    public static final Duration WAIT_TIME = Duration.ofMillis(500); // Verhoog dit als je de verwerking wil volgen in de RabbitMq console
    public static final String EXCHANGE = "Blueprint.Test.Scheduled.Persistent";
    public static final String QUEUE = "MyJob";

    @Autowired
    MockFailingPartyCaller usecase;

    @Autowired
    private PersistentJobProcessor processor;

    @Autowired
    private RabbitTemplate template;

    @Autowired
    ObjectMapper mapper;

    // We versturen MESSAGES messages met een correlationId 0..MESSAGES-1
    // De eerste keer een message met een correlationId verwerkt wordt door MessageReceiverMock, "faalt" de verwerking en komt de message op DLQ
    // Na 3s komt de message terug van DLQ op verwerkings queue
    // De tweede keer een message met een correlationId verwerkt wordt door MessageReceiverMock, "faalt" de verwerking en komt de message opnieuw op DLQ
    // De derde keer lukt het wel
    // We verwachten dus dat elke correlation id drie keer voorkomt in de verwerkte messages

    @SneakyThrows
    @Test
    public void failedMessageGoToDLQ() {
        var senderId = UUID.randomUUID().toString();
        usecase.expect(MESSAGES, ITERATIONS, senderId, WAIT_TIME);

        template.setExchange(EXCHANGE);

        var runnable = new Runnable() {
            @Override
            public void run() {
                sendMessages(senderId);
            }
        };
        var thread = new Thread(runnable);
        thread.start();
        thread.join();
        System.out.println("Waiting at end of test");
        var succeeded = usecase.waitUntilAllMessagesReceived();
        assertThat(succeeded).isTrue();

        var messages = usecase.getSeenMessages();
        assertThat(messages.size()).isEqualTo(MESSAGES * ITERATIONS);

        for (var correlationId = 0; correlationId < MESSAGES; correlationId++) {
            assertWasProcessed(messages, Integer.toString(correlationId), ITERATIONS);
        }
    }

    private void assertWasProcessed(List<String> messages, String x, int iterations) {
        assertThat(messages.stream().filter(m -> x.equalsIgnoreCase(m)).count()).isEqualTo(iterations);
    }

    private void sendMessages(String senderId) {
        for (var correlationId = 0; correlationId < MESSAGES; correlationId++) {
            try {
                System.out.println("Send message with correlation id " + correlationId);
                var msg = makeRabbitMqMessage(buildMessage(correlationId, senderId));
                this.template.send(QUEUE, msg);
                Thread.sleep(WAIT_TIME.toMillis());
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private Message makeRabbitMqMessage(PersistentJob message) throws JsonProcessingException {
        var props = new MessageProperties();
        props.setContentType("application/json");
        var json = mapper.writeValueAsString(message);
        return new Message(json.getBytes(StandardCharsets.UTF_8), props);
    }

    private PersistentJob buildMessage(int correlationId, String senderid) {
        return PersistentJob.builder()
                .persistent(true)
                .expiresAt(Instant.now().plus(Duration.ofHours(1)))
                .counter(correlationId)
                .body(senderid)
                .build();
    }
}
