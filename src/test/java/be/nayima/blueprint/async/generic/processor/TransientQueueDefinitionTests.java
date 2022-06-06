package be.nayima.blueprint.async.generic.processor;

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

public class TransientQueueDefinitionTests {

    public static final String EXCHANGE_NAME = "Blueprint.Scheduled.Basic";
    public static final String SUPPLIER_NAME = "basicJobSupplier";
    public static final String QUEUE_NAME = "MyJob";
    public static final String PROCESSOR_NAME = "basicJobProcessor";

    @Test
    public void testConfigureSupplier() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setMaxAttempts(2).setConcurrency(1);
        var producer = new ProducerDefinition(SUPPLIER_NAME, queue);

        assertThat(producer.bindingName()).isEqualTo(SUPPLIER_NAME + "-out-0");

        Properties props = new Properties();
        producer.configure(props);

        assertThat(props.size()).isEqualTo(3);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.producer.requiredGroups")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.autobindDlq")).isEqualTo("false");

    }

    @Test
    public void testConfigureSupplierWithSingleActiveConsumer() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setSingleActiveConsumer().setConcurrency(1);
        var producer = new ProducerDefinition(SUPPLIER_NAME, queue);

        assertThat(producer.bindingName()).isEqualTo(SUPPLIER_NAME + "-out-0");

        Properties props = new Properties();
        producer.configure(props);

        assertThat(props.size()).isEqualTo(4);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobSupplier-out-0.producer.requiredGroups")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.autobindDlq")).isEqualTo("false");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobSupplier-out-0.producer.singleActiveConsumer")).isEqualTo("true");

    }

    @Test
    public void testConfigureProcessor() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setMaxAttempts(2).setConcurrency(5);
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue);
        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props);

        assertThat(props.size()).isEqualTo(5);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.max-attempts")).isEqualTo("2");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("5");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("false");
    }

    @Test
    public void testConfigureProcessorWithSingleActiveConsumer() {
        var queue = new TransientQueueDefinition(EXCHANGE_NAME, QUEUE_NAME).setSingleActiveConsumer().setConcurrency(5);
        var consumer = new ConsumerDefinition(PROCESSOR_NAME, queue);
        assertThat(consumer.bindingName()).isEqualTo(PROCESSOR_NAME + "-in-0");

        Properties props = new Properties();
        consumer.configure(props);

        assertThat(props.size()).isEqualTo(6);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.destination")).isEqualTo(EXCHANGE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.group")).isEqualTo(QUEUE_NAME);
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.max-attempts")).isEqualTo("1");
        assertThat(props.getProperty("spring.cloud.stream.bindings.basicJobProcessor-in-0.consumer.concurrency")).isEqualTo("1");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.autobindDlq")).isEqualTo("false");
        assertThat(props.getProperty("spring.cloud.stream.rabbit.bindings.basicJobProcessor-in-0.consumer.singleActiveConsumer")).isEqualTo("true");
    }
}
