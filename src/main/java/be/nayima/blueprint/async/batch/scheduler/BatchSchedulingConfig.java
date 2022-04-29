package be.nayima.blueprint.async.batch.scheduler;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Data
@Configuration
@ConfigurationProperties(prefix = "blueprint.batch.schedules")
public class BatchSchedulingConfig {
    // CRON schedule for putting batch jobs onto the queue
    private String batchJobCreationSchedule;

}
