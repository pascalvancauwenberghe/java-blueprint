package be.nayima.blueprint.async.generic.processor;

import java.time.Duration;
import java.util.Properties;

import static be.nayima.blueprint.async.generic.processor.QueueDefinition.SPRING_CLOUD_STREAM_BINDINGS;

public class ConsumerDefinition implements QueueFunctionDefinition {
    private final String name;
    private final QueueDefinition queueDefinition;
    private final String suffix;

    private int maxAttempts = 3;
    private Duration backoffInitial = Duration.ofSeconds(1);
    private Duration backoffMaximum = Duration.ofSeconds(10);
    private double backoffMultiplier = 2.0;

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

    public void configure(Properties properties) {
        queueDefinition.configureConsumer(name, suffix, properties);

        properties.put(SPRING_CLOUD_STREAM_BINDINGS + bindingName() + ".consumer.max-attempts", Integer.toString(maxAttempts));
        if (maxAttempts > 1) {
            properties.put(SPRING_CLOUD_STREAM_BINDINGS + bindingName() + ".consumer.backOffInitialInterval", Long.toString(backoffInitial.toMillis()));
            properties.put(SPRING_CLOUD_STREAM_BINDINGS + bindingName() + ".consumer.backOffMaxInterval", Long.toString(backoffMaximum.toMillis()));
            properties.put(SPRING_CLOUD_STREAM_BINDINGS + bindingName() + ".consumer.backOffMultiplier", Double.toString(backoffMultiplier));
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
