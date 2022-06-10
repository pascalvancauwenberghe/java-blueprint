
package be.nayima.blueprint.async.persistent.processor;

import be.nayima.blueprint.async.generic.processor.ConsumerDefinition;
import be.nayima.blueprint.async.generic.processor.PersistentQueueDefinition;
import be.nayima.blueprint.async.generic.processor.ProducerDefinition;
import be.nayima.blueprint.async.generic.processor.QueueDefinition;
import be.nayima.blueprint.async.generic.processor.QueueFunctionDefinition;
import be.nayima.blueprint.async.generic.processor.TransientQueueDefinition;
import be.nayima.blueprint.async.persistent.message.PersistentJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

@Configuration
public class PersistentStreamConfig {

    static QueueDefinition persistentJobs = new PersistentQueueDefinition("Blueprint.Scheduled.Persistent", "MyJob", Duration.ofSeconds(5)).setConcurrency(1);
    static QueueDefinition graveyard = new TransientQueueDefinition("Blueprint.Scheduled.Persistent.Graveyard", "MyJob");

    static final String PERSISTENT_JOB_PRODUCER = "persistentJobSupplier";
    public static ProducerDefinition persistentJobProducer = new ProducerDefinition(PERSISTENT_JOB_PRODUCER, persistentJobs);

    @Bean
    public Consumer<Message<PersistentJob>> persistentjobProcessor(PersistentJobProcessor service) {
        return service::process;
    }

    static final String PERSISTENT_JOB_PROCESSOR = "persistentjobProcessor";
    public static ConsumerDefinition persistentJobConsumer = new ConsumerDefinition(PERSISTENT_JOB_PROCESSOR, persistentJobs).setMaxAttempts(2);
    public static ProducerDefinition persistentJobGraveyardProducer = new ProducerDefinition(PERSISTENT_JOB_PROCESSOR, graveyard);

    public static List<QueueFunctionDefinition> allFunctions() {
        return List.of(persistentJobProducer, persistentJobConsumer, persistentJobGraveyardProducer);
    }
}


