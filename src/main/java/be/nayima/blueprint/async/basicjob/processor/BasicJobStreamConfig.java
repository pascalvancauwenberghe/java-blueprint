package be.nayima.blueprint.async.basicjob.processor;

import be.nayima.blueprint.async.basicjob.message.BasicJob;
import be.nayima.blueprint.async.basicjob.usecase.PerformBasicJob;
import be.nayima.blueprint.async.generic.message.DroppableJob;
import be.nayima.blueprint.async.generic.processor.ConsumerDefinition;
import be.nayima.blueprint.async.generic.processor.DroppableJobExecutor;
import be.nayima.blueprint.async.generic.processor.ProducerDefinition;
import be.nayima.blueprint.async.generic.processor.QueueDefinition;
import be.nayima.blueprint.async.generic.processor.QueueFunctionDefinition;
import be.nayima.blueprint.async.generic.processor.TransientQueueDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.function.Consumer;

@Configuration
public class BasicJobStreamConfig {

    static QueueDefinition basicJobs = new TransientQueueDefinition("Blueprint.Scheduled.Basic", "MyJob").setConcurrency(1);

    static final String BASIC_JOB_PRODUCER = "basicJobSupplier";
    public static ProducerDefinition basicJobProducer = new ProducerDefinition(BASIC_JOB_PRODUCER, basicJobs);


    // Use the function definition in application.yml for basicjob-processor reading from basicjob-processor-in-0
    // The DroppableJobExecutor is implemented by usecase.PerformBasicJob
    @Bean
    public Consumer<DroppableJob<BasicJob>> basicjobProcessor(DroppableJobExecutor<BasicJob, PerformBasicJob> service) {
        return service::process;
    }

    static final String BASIC_JOB_CONSUMER = "basicjobProcessor";
    public static ConsumerDefinition basicJobConsumer = new ConsumerDefinition(BASIC_JOB_CONSUMER, basicJobs).setMaxAttempts(2);

    public static List<QueueFunctionDefinition> allFunctions() {
        return List.of(basicJobProducer, basicJobConsumer);
    }
}

