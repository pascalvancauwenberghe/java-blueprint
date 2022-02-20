package be.nayima.blueprint.async.asyncblueprint.processor;

import be.nayima.blueprint.async.asyncblueprint.message.BasicJob;
import be.nayima.blueprint.async.asyncblueprint.message.DroppableJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class StreamConfig {
    @Bean
    public Consumer<DroppableJob<BasicJob>> basicjobProcessor(BasicJobExecutor service) {
        return service::process;
    }

}

