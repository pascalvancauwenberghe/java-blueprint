package be.nayima.blueprint.async.asyncblueprint.processor;

import be.nayima.blueprint.async.asyncblueprint.message.DroppableJob;
import be.nayima.blueprint.async.asyncblueprint.usecase.CreateBasicJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.Duration;
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

    public void supplyJob(Duration ttl) {
        var job = DroppableJob.builder()
                .name("BasicJob")
                .ttl(Instant.now().plus(ttl))
                .body(creator.create()).build();
        log.info("Sending message {} which expires at {}", job.getName(), formatter.format(job.getTtl()));
        streamBridge.send(OUTPUT_BINDING, job);
    }
}
