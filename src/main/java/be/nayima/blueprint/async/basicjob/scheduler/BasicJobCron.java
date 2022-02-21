package be.nayima.blueprint.async.basicjob.scheduler;

import be.nayima.blueprint.async.basicjob.processor.BasicJobSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
@Profile({ "!test" })
public class BasicJobCron {
    private final BasicJobSupplier supplier;
    private final SchedulingConfig config;

    // Add one job at a time
    @Scheduled(cron = "${blueprint.schedules.basic-job-creation-schedule}")
    public void generateJob() {
        supplier.supplyJob(Instant.now().plus(Duration.ofSeconds(config.getBasicJobCreationTtl())));
    }

    // Add a batch of 5-10 jobs
    @Scheduled(cron = "${blueprint.schedules.basic-job-batch-creation-schedule}")
    public void generateManyJobs() {
        supplier.supplyManyJobs(Instant.now().plus(Duration.ofSeconds(config.getBasicJobBatchCreationTtl())));
    }

}
