package be.nayima.blueprint.async.generic.processor;

import lombok.Getter;

import java.util.Properties;

@Getter
public abstract class QueueDefinition {
    String SPRING_CLOUD_STREAM_BINDINGS = "spring.cloud.stream.bindings.";
    String SPRING_CLOUD_STREAM_RABBIT_BINDINGS = "spring.cloud.stream.rabbit.bindings.";

    private final String exchange;
    private final String queue;
    private int maxAttempts;
    private int concurrency;
    private boolean singleActiveConsumer;

    public QueueDefinition(String exchange, String queue) {
        this.exchange = exchange;
        this.queue = queue;
        this.maxAttempts = 1;
        this.concurrency = 1;
        this.singleActiveConsumer = false;
    }

    public QueueDefinition setMaxAttempts(int attempts) {
        this.maxAttempts = attempts;
        return this;
    }

    public QueueDefinition setConcurrency(int processors) {
        this.concurrency = processors;
        return this;
    }

    public QueueDefinition setSingleActiveConsumer() {
        this.singleActiveConsumer = true;
        return this;
    }

    private void validateValues() {
        if (singleActiveConsumer) {
            concurrency = 1;
        }
    }

    protected void configureConsumer(String processor, String suffix, Properties properties) {
        validateValues();
        String inputBinding = processor + suffix;
        properties.put(SPRING_CLOUD_STREAM_BINDINGS + inputBinding + ".destination", getExchange());
        properties.put(SPRING_CLOUD_STREAM_BINDINGS + inputBinding + ".group", getQueue());
        properties.put(SPRING_CLOUD_STREAM_BINDINGS + inputBinding + ".consumer.max-attempts", Integer.toString(maxAttempts));
        properties.put(SPRING_CLOUD_STREAM_BINDINGS + inputBinding + ".consumer.concurrency", Integer.toString(concurrency));
        if (singleActiveConsumer) {
            properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + inputBinding + ".consumer.singleActiveConsumer", "true");
        }
    }

    protected void configureProducer(String supplier, String suffix, Properties properties) {
        validateValues();
        String outputBinding = supplier + suffix;
        properties.put(SPRING_CLOUD_STREAM_BINDINGS + outputBinding + ".destination", getExchange());
        properties.put(SPRING_CLOUD_STREAM_BINDINGS + outputBinding + ".producer.requiredGroups", getQueue());
        if (singleActiveConsumer) {
            properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + outputBinding + ".producer.singleActiveConsumer", "true");
        }
    }
}
