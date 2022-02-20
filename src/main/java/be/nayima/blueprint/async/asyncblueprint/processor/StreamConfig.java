package be.nayima.blueprint.async.asyncblueprint.processor;

import be.nayima.blueprint.async.asyncblueprint.message.basicjob.BasicJob;
import be.nayima.blueprint.async.asyncblueprint.message.generic.DroppableJob;
import be.nayima.blueprint.async.asyncblueprint.processor.generic.DroppableJobExecutor;
import be.nayima.blueprint.async.asyncblueprint.usecase.basicjob.PerformBasicJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class StreamConfig {
    // Use the function definition in application.yml for basicjob-processor reading from basicjob-processor-in-0
    // The DroppableJobExecutor is implemented by usecase.PerformBasicJob
    @Bean
    public Consumer<DroppableJob<BasicJob>> basicjobProcessor(DroppableJobExecutor<BasicJob, PerformBasicJob> service) {
        return service::process;
    }

}

