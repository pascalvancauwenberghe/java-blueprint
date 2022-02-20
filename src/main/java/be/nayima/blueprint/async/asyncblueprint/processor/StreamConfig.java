package be.nayima.blueprint.async.asyncblueprint.processor;

import be.nayima.blueprint.async.asyncblueprint.message.basicjob.BasicJob;
import be.nayima.blueprint.async.asyncblueprint.message.generic.DroppableJob;
import be.nayima.blueprint.async.asyncblueprint.processor.generic.DroppableJobExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class StreamConfig {
    @Bean
    public Consumer<DroppableJob<BasicJob>> basicjobProcessor(DroppableJobExecutor<BasicJob> service) {
        return service::process;
    }

}

