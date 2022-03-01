package be.nayima.blueprint.async.persistent.scheduler;

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
public class PersistentJobCron {
    private final PersistentJobSupplier supplier;
    private final PersistentSchedulingConfig config;

    // Add one job at a time
    @Scheduled(fixedRate = 2000)
    public void generateJob() {
        supplier.supplyJob(Instant.now().plus(Duration.ofSeconds(config.getPersistentJobCreationTtl())));
    }

}
