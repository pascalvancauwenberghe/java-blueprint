package be.nayima.blueprint.async.basicjob.scheduler;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Data
@Configuration
@ConfigurationProperties(prefix = "blueprint.basicjob.schedules")
public class SchedulingConfig {
    // CRON schedule for putting BasicJob onto the queue
    private String basicJobCreationSchedule;
    // TTL for the BasicJobs on the queue
    private int basicJobCreationTtl;
    // TTL for the BasicJobs that have been added in batch on the queue
    private int basicJobBatchCreationTtl;
    // How many seconds it takes to process one BasicJob. Set this higher than the frequency of the CRON schedule and the TTL to mimic fast producer-slow consumer
    private int basicJobProcessingInterval;
}
