package be.nayima.blueprint.async.generic.processor;

import java.time.Duration;
import java.util.Properties;

import static be.nayima.blueprint.async.generic.processor.QueueDefinition.SPRING_CLOUD_STREAM_BINDINGS;
import static be.nayima.blueprint.async.generic.processor.QueueDefinition.SPRING_CLOUD_STREAM_RABBIT_BINDINGS;

public class ConsumerDefinition implements QueueFunctionDefinition {
    private final String name;
    private final QueueDefinition queueDefinition;
    private final String suffix;

    private int maxAttempts = 3;
    private Duration backoffInitial = Duration.ofSeconds(1);
    private Duration backoffMaximum = Duration.ofSeconds(10);
    private double backoffMultiplier = 2.0;

    private int batchSize;
    private Duration receiveTimeout = Duration.ofSeconds(1);

    public ConsumerDefinition(String name, QueueDefinition queueDefinition) {
        this.name = name;
        this.queueDefinition = queueDefinition;
        this.suffix = "-in-0";
    }

    public ConsumerDefinition setMaxAttempts(int attempts) {
        this.maxAttempts = attempts;
        return this;
    }

    public ConsumerDefinition setRetrySchedule(Duration backoffInitial, Duration backoffMaximum, double backoffMultiplier) {
        this.backoffInitial = backoffInitial;
        this.backoffMaximum = backoffMaximum;
        this.backoffMultiplier = backoffMultiplier;
        return this;
    }

    public ConsumerDefinition setBatchMode(int batchSize, Duration receiveTimeout) {
        this.batchSize = batchSize;
        this.receiveTimeout = receiveTimeout;
        return this;
    }

    public void configure(Properties properties, boolean testEnvironment) {
        queueDefinition.configureConsumer(name, suffix, properties, testEnvironment);

        String streamBindings = SPRING_CLOUD_STREAM_BINDINGS + bindingName();
        String rabbitBindings = SPRING_CLOUD_STREAM_RABBIT_BINDINGS + bindingName();

        properties.put(streamBindings + ".consumer.max-attempts", Integer.toString(maxAttempts));
        if (maxAttempts > 1) {
            properties.put(streamBindings + ".consumer.backOffInitialInterval", Long.toString(backoffInitial.toMillis()));
            properties.put(streamBindings + ".consumer.backOffMaxInterval", Long.toString(backoffMaximum.toMillis()));
            properties.put(streamBindings + ".consumer.backOffMultiplier", Double.toString(backoffMultiplier));
        }

        if (batchSize > 1) {
            properties.put(streamBindings + ".consumer.batchMode", "true");
            properties.put(rabbitBindings + ".consumer.enableBatching", "true");
            properties.put(rabbitBindings + ".consumer.batchSize", Integer.toString(batchSize));
            properties.put(rabbitBindings + ".consumer.receiveTimeout", Long.toString(receiveTimeout.toMillis()));
        }
    }

    @Override
    public String processorName() {
        return name;
    }

    @Override
    public String bindingName() {
        return name + suffix;
    }
}
