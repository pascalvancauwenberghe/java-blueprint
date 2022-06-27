package be.nayima.blueprint.async.generic.processor;

import java.time.Duration;
import java.util.Properties;

public abstract class QueueDefinition {
    public static String SPRING_CLOUD_STREAM_BINDINGS = "spring.cloud.stream.bindings.";
    public static String SPRING_CLOUD_STREAM_RABBIT_BINDINGS = "spring.cloud.stream.rabbit.bindings.";

    private final String exchange;
    private final String queue;

    private boolean singleActiveConsumer;
    private Duration ttl;

    public QueueDefinition(String exchange, String queue) {
        this.exchange = exchange;
        this.queue = queue;
        this.singleActiveConsumer = false;
        this.ttl = null;
    }

    private String getExchange(boolean testEnvironment) {
        return exchange + (testEnvironment ? ".Test" : "");
    }

    private String getQueue(boolean testEnvironment) {
        return queue + (testEnvironment ? ".Test" : "");
    }

    public QueueDefinition withSingleActiveConsumer() {
        this.singleActiveConsumer = true;
        return this;
    }

    public boolean isSingleActiveConsumer() {
        return this.singleActiveConsumer ;
    }

    public QueueDefinition withTimeToLive(Duration ttl) {
        this.ttl = ttl;
        return this;
    }

    private void validateValues() {
    }

    protected void configureConsumer(String processor, String suffix, Properties properties, boolean testEnvironment) {
        validateValues();
        String inputBinding = processor + suffix;
        properties.put(SPRING_CLOUD_STREAM_BINDINGS + inputBinding + ".destination", getExchange(testEnvironment));
        properties.put(SPRING_CLOUD_STREAM_BINDINGS + inputBinding + ".group", getQueue(testEnvironment));

        if (singleActiveConsumer) {
            properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + inputBinding + ".consumer.singleActiveConsumer", "true");
        }
        if (ttl != null) {
            properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + inputBinding + ".consumer.ttl", Long.toString(ttl.toMillis()));
        }

    }


    protected void configureProducer(String supplier, String suffix, Properties properties, boolean testEnvironment) {
        validateValues();
        String outputBinding = supplier + suffix;
        properties.put(SPRING_CLOUD_STREAM_BINDINGS + outputBinding + ".destination", getExchange(testEnvironment));
        properties.put(SPRING_CLOUD_STREAM_BINDINGS + outputBinding + ".producer.requiredGroups", getQueue(testEnvironment));
        if (singleActiveConsumer) {
            properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + outputBinding + ".producer.singleActiveConsumer", "true");
        }
        if (ttl != null) {
            properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + outputBinding + ".producer.ttl", Long.toString(ttl.toMillis()));
        }

    }
}
