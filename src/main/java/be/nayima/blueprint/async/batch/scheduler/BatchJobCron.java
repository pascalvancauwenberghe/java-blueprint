package be.nayima.blueprint.async.batch.scheduler;

import be.nayima.blueprint.async.batch.processor.BatchJobSupplier;
import be.nayima.blueprint.async.persistent.processor.PersistentJobSupplier;
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
public class BatchJobCron {
    private final BatchJobSupplier supplier;
    private final BatchSchedulingConfig config;


    // Add one job at a time
    @Scheduled(fixedDelay = 8*1000)
    public void generateJob() {
        supplier.supplyJob();
    }


}
