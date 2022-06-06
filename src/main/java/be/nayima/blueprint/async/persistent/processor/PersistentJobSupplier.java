package be.nayima.blueprint.async.persistent.processor;

import be.nayima.blueprint.async.persistent.message.PersistentJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersistentJobSupplier {
    private static int counter = 1;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));

    // StreamBridge should be an interface instead of implementation, so that we can mock it in unit tests
    private final StreamBridge streamBridge;

    public void supplyJob(Instant expiresAt) {
        var id = counter;
        counter++;
        var persistentJob = PersistentJob.builder().body(UUID.randomUUID().toString()).counter(id).persistent(id % 2 == 1).expiresAt(Instant.now().plusSeconds(3)).build();
        sendPersistentJob(persistentJob);
    }

    private void sendPersistentJob(PersistentJob job) {

        //log.info("SEND. Message {} done processing at {}.", job.getCounter(), Instant.now());
        streamBridge.send(PersistentStreamConfig.persistentJobProducer.bindingName(), job);
    }

}
