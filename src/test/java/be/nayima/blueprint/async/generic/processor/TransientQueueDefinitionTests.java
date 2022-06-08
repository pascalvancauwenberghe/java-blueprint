package be.nayima.blueprint.async.generic.processor;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class TransientQueueDefinitionTests {

    public static final String EXCHANGE_NAME = "Blueprint.Scheduled.Basic";
    public static final String SUPPLIER_NAME = "basicJobSupplier";
    public static final String QUEUE_NAME = "MyJob";
    public static final String PROCESSOR_NAME = "basicJobProcessor";

    @Test
    public void testConfigureSupplier() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setConcurrency(1);
        var producer = new ProducerDefinition(SUPPLIER_NAME, queue);

        assertThat(producer.bindingName()).isEqualTo(SUPPLIER_NAME + "-out-0");

        Properties props = new Properties();
        producer.configure(props, false);

        assertThat(props.size()).isEqualTo(3);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.producer.requiredGroups")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.autobindDlq")).isEqualTo("false");

    }

    @Test
    public void testConfigureSupplierWithTtl() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setTimeToLive(Duration.ofMinutes(5));
        var producer = new ProducerDefinition(SUPPLIER_NAME, queue);

        assertThat(producer.bindingName()).isEqualTo(SUPPLIER_NAME + "-out-0");

        Properties props = new Properties();
        producer.configure(props, false);

        assertThat(props.size()).isEqualTo(4);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.producer.requiredGroups")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.autobindDlq")).isEqualTo("false");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.ttl")).isEqualTo("300000");

    }

    @Test
    public void testConfigureSupplierInTestEnvironment() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setConcurrency(1);
        var producer = new ProducerDefinition(SUPPLIER_NAME, queue);

        assertThat(producer.bindingName()).isEqualTo(SUPPLIER_NAME + "-out-0");

        Properties props = new Properties();
        producer.configure(props, true);

        assertThat(props.size()).isEqualTo(3);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.destination")).isEqualTo(EXCHANGE_NAME + ".Test");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.producer.requiredGroups")).isEqualTo(QUEUE_NAME + ".Test");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.autobindDlq")).isEqualTo("false");

    }

    @Test
    public void testConfigureSupplierWithSingleActiveConsumer() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setSingleActiveConsumer().setConcurrency(1);
        var producer = new ProducerDefinition(SUPPLIER_NAME, queue);

        assertThat(producer.bindingName()).isEqualTo(SUPPLIER_NAME + "-out-0");

        Properties props = new Properties();
        producer.configure(props, false);

        assertThat(props.size()).isEqualTo(4);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.producer.requiredGroups")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.autobindDlq")).isEqualTo("false");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.singleActiveConsumer")).isEqualTo("true");

    }

    @Test
    public void testConfigureProcessor() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setConcurrency(5);
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue).setMaxAttempts(2);
        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props, false);

        assertThat(props.size()).isEqualTo(8);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.max-attempts")).isEqualTo("2");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffInitialInterval")).isEqualTo("1000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMaxInterval")).isEqualTo("10000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMultiplier")).isEqualTo("2.0");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("5");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("false");
    }

    @Test
    public void testConfigureProcessorWithTtl() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setConcurrency(5).setTimeToLive(Duration.ofSeconds(30));
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue).setMaxAttempts(2);
        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props, false);

        assertThat(props.size()).isEqualTo(9);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.max-attempts")).isEqualTo("2");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffInitialInterval")).isEqualTo("1000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMaxInterval")).isEqualTo("10000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMultiplier")).isEqualTo("2.0");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("5");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("false");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.ttl")).isEqualTo("30000");
    }

    @Test
    public void testConfigureProcessorInTestEnvironment() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setConcurrency(5);
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue).setMaxAttempts(2);
        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props, true);

        assertThat(props.size()).isEqualTo(8);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME + ".Test");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME + ".Test");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.max-attempts")).isEqualTo("2");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffInitialInterval")).isEqualTo("1000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMaxInterval")).isEqualTo("10000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMultiplier")).isEqualTo("2.0");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("5");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("false");
    }

    @Test
    public void testConfigureProcessorWithBatch() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setConcurrency(5);
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue).setMaxAttempts(2).setBatchMode(100, Duration.ofSeconds(10));
        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props, false);

        assertThat(props.size()).isEqualTo(12);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.max-attempts")).isEqualTo("2");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffInitialInterval")).isEqualTo("1000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMaxInterval")).isEqualTo("10000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMultiplier")).isEqualTo("2.0");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("5");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.batchMode")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("false");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.enableBatching")).isEqualTo("true");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.batchSize")).isEqualTo("100");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.receiveTimeout")).isEqualTo("10000");
    }

    @Test
    public void testConfigureProcessorWithSingleActiveConsumer() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setSingleActiveConsumer().setConcurrency(5);
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue);
        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props, false);

        assertThat(props.size()).isEqualTo(9);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.max-attempts")).isEqualTo("3");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffInitialInterval")).isEqualTo("1000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMaxInterval")).isEqualTo("10000");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.backOffMultiplier")).isEqualTo("2.0");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("1");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("false");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.singleActiveConsumer")).isEqualTo("true");
    }
}
