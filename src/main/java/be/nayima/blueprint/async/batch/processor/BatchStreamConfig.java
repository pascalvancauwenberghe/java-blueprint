
package be.nayima.blueprint.async.batch.processor;

import be.nayima.blueprint.async.batch.message.BatchJob;
import be.nayima.blueprint.async.batch.message.BatchResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.function.Function;

@Configuration
public class BatchStreamConfig {
    @Bean
    public Function<List<BatchJob>, Message<BatchResult>> batchJobProcessor(BatchhobProcessor service) {
        return service::process;
    }

}


