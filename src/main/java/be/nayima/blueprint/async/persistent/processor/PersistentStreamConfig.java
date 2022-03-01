
package be.nayima.blueprint.async.persistent.processor;

import be.nayima.blueprint.async.persistent.message.PersistentJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Configuration
public class PersistentStreamConfig {
    @Bean
    public Consumer<Message<PersistentJob>> persistentjobProcessor(PersistentJobProcessor service) {
        return service::process;
    }

}


