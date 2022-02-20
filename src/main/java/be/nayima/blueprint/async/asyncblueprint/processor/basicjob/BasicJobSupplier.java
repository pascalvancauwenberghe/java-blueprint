package be.nayima.blueprint.async.asyncblueprint.processor.basicjob;

import be.nayima.blueprint.async.asyncblueprint.message.basicjob.BasicJob;
import be.nayima.blueprint.async.asyncblueprint.message.generic.DroppableJob;
import be.nayima.blueprint.async.asyncblueprint.usecase.basicjob.CreateBasicJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicJobSupplier {
    public static final String OUTPUT_BINDING = "basicjobsOut";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));

    private final StreamBridge streamBridge;
    private final CreateBasicJob creator;

    public void supplyJob(Instant expiresAt) {
        var basicJob = creator.create();
        var job = DroppableJob.builder()
                .name("BasicJob")
                .ttl(expiresAt)
                .body(basicJob).build();
        log.info("Sending message {} which expires at {}", job.getName(), formatter.format(job.getTtl()));
        streamBridge.send(OUTPUT_BINDING, job);
    }
}
