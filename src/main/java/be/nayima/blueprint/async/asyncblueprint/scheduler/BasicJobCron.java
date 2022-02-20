package be.nayima.blueprint.async.asyncblueprint.scheduler;

import be.nayima.blueprint.async.asyncblueprint.config.SchedulingConfig;
import be.nayima.blueprint.async.asyncblueprint.processor.BasicJobSupplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicJobCron {
    private final BasicJobSupplier supplier;
    private final SchedulingConfig config;

    @Scheduled(cron = "${blueprint.schedules.basic-job-creation-schedule}")
    public void generateJob() {
        supplier.supplyJob(Duration.ofSeconds(config.getBasicJobCreationTtl()));
    }

}
