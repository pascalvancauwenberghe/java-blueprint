package be.nayima.blueprint.async.asyncblueprint.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Data
@Configuration
@ConfigurationProperties(prefix = "blueprint.schedules")
public class SchedulingConfig {
    private String basicJobCreationSchedule;
    private int basicJobCreationTtl;
    private int basicJobProcessingInterval;
}
