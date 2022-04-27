package be.nayima.blueprint.async.basicjob.mock;

import be.nayima.blueprint.async.persistent.connector.CallFailedException;
import be.nayima.blueprint.async.persistent.usecase.FailingPartyCaller;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@Primary
@Profile({"local-rabbitmq"})
public class MockFailingPartyCaller implements FailingPartyCaller {
    private static final int DEFAULT_SUCCESSFUL_MESSAGES = 3;

    private List<String> seenMessages = new ArrayList<String>();
    private CountDownLatch countDownLatch = new CountDownLatch(DEFAULT_SUCCESSFUL_MESSAGES);
    private String expectedSenderId = "";
    private int expectedMessages = DEFAULT_SUCCESSFUL_MESSAGES;
    private int expectedIterations = 2;
    private Duration waitTime = Duration.ofMillis(500);

    @Override
    public void call(String request, int counter) throws CallFailedException {
        var correlationId = Integer.toString(counter);
        String senderId = request;

        // Skip eventuele resterende messages van vorige test
        if (!expectedSenderId.equalsIgnoreCase(senderId)) {
            System.err.println("Dropping old messages with sender id " + senderId);
            return;
        }

        try {
            Thread.sleep(waitTime.toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // De processing blijft falen tot het de expectedIterations keer wel lukt

        seenMessages.add(correlationId);
        var iterations = seenMessages.stream().filter(m -> m.equalsIgnoreCase(correlationId)).count();
        if (iterations < expectedIterations) {
            System.err.println("Failing message " + correlationId);
            throw new CallFailedException("Can't process " + correlationId);
        } else {
            System.out.println("Processing message " + correlationId);
            countDownLatch.countDown();
        }
    }

    public List<String> getSeenMessages() {
        return seenMessages;
    }

    public void expect(int expectedMessages, int iterations, String expectedSenderId, Duration waitTime) {
        seenMessages = new ArrayList<String>();
        this.expectedMessages = expectedMessages;
        this.expectedIterations = iterations;
        this.expectedSenderId = expectedSenderId;
        countDownLatch = new CountDownLatch(this.expectedMessages);
        this.waitTime = waitTime;
    }

    public boolean waitUntilAllMessagesReceived() {
        try {
            return countDownLatch.await(expectedMessages * expectedIterations * waitTime.toMillis() * 3 * 3, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
