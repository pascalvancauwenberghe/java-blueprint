
package be.nayima.blueprint.async.batch.processor;

import be.nayima.blueprint.async.batch.message.BatchJob;
import be.nayima.blueprint.async.batch.message.BatchResult;
import be.nayima.blueprint.async.generic.processor.ConsumerDefinition;
import be.nayima.blueprint.async.generic.processor.PersistentQueueDefinition;
import be.nayima.blueprint.async.generic.processor.ProducerDefinition;
import be.nayima.blueprint.async.generic.processor.QueueDefinition;
import be.nayima.blueprint.async.generic.processor.QueueFunctionDefinition;
import be.nayima.blueprint.async.generic.processor.TransientQueueDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

@Configuration
public class BatchStreamConfig {
    static QueueDefinition batchJobs = new PersistentQueueDefinition("Blueprint.Scheduled.Batch", "MyJob", Duration.ofSeconds(10)).withSingleActiveConsumer();
    static QueueDefinition batchResult = new TransientQueueDefinition("Blueprint.Scheduled.BatchResult", "MyJob");

    static final String BATCH_JOB_PRODUCER = "batchJobSupplier";
    public static ProducerDefinition batchJobProducer = new ProducerDefinition(BATCH_JOB_PRODUCER, batchJobs);

    @Bean
    public Function<List<BatchJob>, Message<BatchResult>> batchJobProcessor(BatchProcessor service) {
        return service::process;
    }

    static final String BATCH_JOB_CONSUMER = "batchJobProcessor";
    public static ConsumerDefinition batchJobProcessor = new ConsumerDefinition(BATCH_JOB_CONSUMER, batchJobs).withMaxAttempts(2).withBatchMode(50, Duration.ofSeconds(10)).withConcurrency(1);
    public static ProducerDefinition batchJobResults = new ProducerDefinition(BATCH_JOB_CONSUMER, batchResult);

    public static List<QueueFunctionDefinition> allFunctions() {
        return List.of(batchJobProducer, batchJobProcessor, batchJobResults);
    }

}


