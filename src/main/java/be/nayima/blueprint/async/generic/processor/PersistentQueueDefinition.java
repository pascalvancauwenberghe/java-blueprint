package be.nayima.blueprint.async.generic.processor;

import java.time.Duration;
import java.util.Properties;

public class PersistentQueueDefinition extends QueueDefinition {
    private final Duration retryInterval;

    public PersistentQueueDefinition(String exchange, String queue, Duration retryInterval) {
        super(exchange, queue);
        this.retryInterval = retryInterval;
    }

    @Override
    public void configureConsumer(String processor, String suffix, Properties properties, boolean testEnvironment) {
        super.configureConsumer(processor, suffix, properties, testEnvironment);

        String inputBinding = processor + suffix;

        properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + inputBinding + ".consumer.autobindDlq", "true");
        properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + inputBinding + ".consumer.dlqTtl", Long.toString(retryInterval.toMillis()));
        properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + inputBinding + ".consumer.dlqDeadLetterExchange", "");
    }

    @Override
    public void configureProducer(String supplier, String suffix, Properties properties, boolean testEnvironment) {
        super.configureProducer(supplier, suffix, properties, testEnvironment);

        String outputBinding = supplier + suffix;
        properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + outputBinding + ".producer.autobindDlq", "true");
        properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + outputBinding + ".producer.dlqTtl", Long.toString(retryInterval.toMillis()));
        properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + outputBinding + ".producer.dlqDeadLetterExchange", "");
    }
}
