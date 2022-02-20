package be.nayima.blueprint.async.asyncblueprint.config;

import be.nayima.blueprint.async.asyncblueprint.message.BasicJob;
import be.nayima.blueprint.async.asyncblueprint.processor.BasicJobExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class StreamConfig {
    @Bean
    public Consumer<BasicJob> processor(BasicJobExecutor service) {
        return service::process;
    }
}

