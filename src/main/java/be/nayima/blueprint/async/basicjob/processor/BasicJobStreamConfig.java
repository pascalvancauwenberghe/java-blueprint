package be.nayima.blueprint.async.basicjob.processor;

import be.nayima.blueprint.async.basicjob.message.BasicJob;
import be.nayima.blueprint.async.basicjob.usecase.PerformBasicJob;
import be.nayima.blueprint.async.generic.message.DroppableJob;
import be.nayima.blueprint.async.generic.processor.DroppableJobExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class BasicJobStreamConfig {
    // Use the function definition in application.yml for basicjob-processor reading from basicjob-processor-in-0
    // The DroppableJobExecutor is implemented by usecase.PerformBasicJob
    @Bean
    public Consumer<DroppableJob<BasicJob>> basicjobProcessor(DroppableJobExecutor<BasicJob, PerformBasicJob> service) {
        return service::process;
    }


}

