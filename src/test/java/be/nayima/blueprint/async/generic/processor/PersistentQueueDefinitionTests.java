package be.nayima.blueprint.async.generic.processor;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class PersistentQueueDefinitionTests {

    public static final String EXCHANGE_NAME = "Blueprint.Scheduled.Persistent";
    public static final String QUEUE_NAME = "MyJob";
    public static final String SUPPLIER_NAME = "basicJobSupplier";
    public static final String SUFFIX = "graveyard";
    public static final String PROCESSOR_NAME = "basicJobProcessor";

    @Test
    public void testConfigureSupplier() {
        var queue = new PersistentQueueDefinition(EXCHANGE_NAME, QUEUE_NAME, Duration.ofSeconds(3));
        var producer = new ProducerDefinition(SUPPLIER_NAME, queue);

        assertThat(producer.bindingName()).isEqualTo(SUPPLIER_NAME + "-out-0");

        Properties props = new Properties();
        producer.configure(props, false);

        assertThat(props.size()).isEqualTo(5);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.producer.requiredGroups")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.autobindDlq")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.dlqTtl")).isEqualTo("3000");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.dlqDeadLetterExchange")).isEqualTo("");
    }

    @Test
    public void testConfigureSupplierInTestEnvironment() {
        var queue = new PersistentQueueDefinition(EXCHANGE_NAME, QUEUE_NAME, Duration.ofSeconds(3));
        var producer = new ProducerDefinition(SUPPLIER_NAME, queue);

        assertThat(producer.bindingName()).isEqualTo(SUPPLIER_NAME + "-out-0");

        Properties props = new Properties();
        producer.configure(props, true);

        assertThat(props.size()).isEqualTo(5);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.destination")).isEqualTo(EXCHANGE_NAME + ".Test");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.producer.requiredGroups")).isEqualTo(QUEUE_NAME + ".Test");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.autobindDlq")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.dlqTtl")).isEqualTo("3000");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.dlqDeadLetterExchange")).isEqualTo("");
    }

    @Test
    public void testConfigureSupplierWithSingleActiveConsumer() {
        var queue = new PersistentQueueDefinition(EXCHANGE_NAME, QUEUE_NAME, Duration.ofSeconds(3)).withSingleActiveConsumer();
        var producer = new ProducerDefinition(SUPPLIER_NAME, queue);

        assertThat(producer.bindingName()).isEqualTo(SUPPLIER_NAME + "-out-0");

        Properties props = new Properties();
        producer.configure(props, false);

        assertThat(props.size()).isEqualTo(6);
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.singleActiveConsumer")).isEqualTo("true");
    }

    @Test
    public void testConfigureSupplierWithSuffix() {
        var queue = new PersistentQueueDefinition(EXCHANGE_NAME, QUEUE_NAME, Duration.ofSeconds(3));
        var producer = new ProducerDefinition(SUPPLIER_NAME, queue, SUFFIX);

        assertThat(producer.bindingName()).isEqualTo(SUPPLIER_NAME + "-" + SUFFIX);

        Properties props = new Properties();
        producer.configure(props, false);

        assertThat(props.size()).isEqualTo(5);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-graveyard.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-graveyard.producer.requiredGroups")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-graveyard.producer.autobindDlq")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-graveyard.producer.dlqTtl")).isEqualTo("3000");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-graveyard.producer.dlqDeadLetterExchange")).isEqualTo("");
    }

    @Test
    public void testConfigureProcessor() {
        var queue = new PersistentQueueDefinition(EXCHANGE_NAME, QUEUE_NAME, Duration.ofMinutes(2));
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue).withMaxAttempts(2).withConcurrency(5);

        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props, false);

        assertThat(props.size()).isEqualTo(10);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.maxAttempts")).isEqualTo("2");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffInitialInterval")).isEqualTo("1000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMaxInterval")).isEqualTo("10000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMultiplier")).isEqualTo("2.0");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("5");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.dlqTtl")).isEqualTo("120000");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.dlqDeadLetterExchange")).isEqualTo("");
    }

    @Test
    public void testConfigureProcessorWithBatch() {
        var queue = new PersistentQueueDefinition(EXCHANGE_NAME, QUEUE_NAME, Duration.ofMinutes(2));
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue).withMaxAttempts(2).withBatchMode(50, Duration.ofSeconds(3)).withConcurrency(5);

        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props, false);

        assertThat(props.size()).isEqualTo(14);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.maxAttempts")).isEqualTo("2");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffInitialInterval")).isEqualTo("1000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMaxInterval")).isEqualTo("10000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMultiplier")).isEqualTo("2.0");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("5");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.batchMode")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.dlqTtl")).isEqualTo("120000");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.dlqDeadLetterExchange")).isEqualTo("");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.enableBatching")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.batchSize")).isEqualTo("50");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.receiveTimeout")).isEqualTo("3000");
    }

    @Test
    public void testConfigureProcessorInTestEnvironment() {
        var queue = new PersistentQueueDefinition(EXCHANGE_NAME, QUEUE_NAME, Duration.ofMinutes(2));
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue).withMaxAttempts(2).withConcurrency(5);

        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props, true);

        assertThat(props.size()).isEqualTo(10);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME + ".Test");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME + ".Test");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.maxAttempts")).isEqualTo("2");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffInitialInterval")).isEqualTo("1000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMaxInterval")).isEqualTo("10000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMultiplier")).isEqualTo("2.0");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("5");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.dlqTtl")).isEqualTo("120000");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.dlqDeadLetterExchange")).isEqualTo("");
    }

    @Test
    public void testConfigureProcessorWithRetry() {
        var queue = new PersistentQueueDefinition(EXCHANGE_NAME, QUEUE_NAME, Duration.ofMinutes(2));
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue).withMaxAttempts(10).withRetrySchedule(Duration.ofSeconds(2), Duration.ofMinutes(2), 2.0).withConcurrency(5);

        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props, false);

        assertThat(props.size()).isEqualTo(10);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.maxAttempts")).isEqualTo("10");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffInitialInterval")).isEqualTo("2000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMaxInterval")).isEqualTo("120000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMultiplier")).isEqualTo("2.0");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("5");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.dlqTtl")).isEqualTo("120000");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.dlqDeadLetterExchange")).isEqualTo("");
    }

    @Test
    public void testConfigureProcessorWithSingleActiveConsumer() {
        var queue = new PersistentQueueDefinition(EXCHANGE_NAME, QUEUE_NAME, Duration.ofMinutes(2)).withSingleActiveConsumer();
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue).withConcurrency(6);

        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props, false);

        assertThat(props.size()).isEqualTo(11);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.maxAttempts")).isEqualTo("3");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffInitialInterval")).isEqualTo("1000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMaxInterval")).isEqualTo("10000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMultiplier")).isEqualTo("2.0");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("1");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.dlqTtl")).isEqualTo("120000");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.dlqDeadLetterExchange")).isEqualTo("");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.singleActiveConsumer")).isEqualTo("true");
    }
}
