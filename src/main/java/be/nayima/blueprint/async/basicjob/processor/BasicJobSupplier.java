package be.nayima.blueprint.async.basicjob.processor;

import be.nayima.blueprint.async.basicjob.message.BasicJob;
import be.nayima.blueprint.async.generic.message.DroppableJob;
import be.nayima.blueprint.async.basicjob.usecase.CreateBasicJob;
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
// The BasicJobSupplier creates one BasicJob (via usecase.CreateBasicJob) and puts it on the input queue
// The binding basicJobsOut must be declared in application.yml
public class BasicJobSupplier {
    public static final String OUTPUT_BINDING = "basicJobSupplier-out-0";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));

    // StreamBridge should be an interface instead of implementation, so that we can mock it in unit tests
    private final StreamBridge streamBridge;
    private final CreateBasicJob creator;

    public void supplyJob(Instant expiresAt) {
        var basicJob = creator.create();
        sendBasicJob(basicJob, expiresAt);
    }

    public void supplyManyJobs(Instant expiresAt) {
        var jobs = creator.createBatch();
        log.info("Sending {} messages in batch", jobs.size());
        for (BasicJob job : jobs) {
            sendBasicJob(job, expiresAt);
        }

    }

    private void sendBasicJob(BasicJob basicJob, Instant expiresAt) {
        var job = DroppableJob.builder()
                .name("BasicJob")
                .expiresAt(expiresAt)
                .body(basicJob).build();
        log.info("Sending message {} which expires at {}", job.getName(), formatter.format(job.getExpiresAt()));
        streamBridge.send(OUTPUT_BINDING, job);
    }

}
