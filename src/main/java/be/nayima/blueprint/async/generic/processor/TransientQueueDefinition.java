package be.nayima.blueprint.async.generic.processor;

import java.util.Properties;

public class TransientQueueDefinition extends QueueDefinition {
    public TransientQueueDefinition(String exchange, String queue) {
        super(exchange, queue);
    }

    @Override
    public void configureConsumer(String processor, String suffix, Properties properties, boolean testEnvironment) {
        super.configureConsumer(processor, suffix, properties, testEnvironment);

        String inputBinding = processor + suffix;

        properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + inputBinding + ".consumer.autobindDlq", "false");
    }

    @Override
    public void configureProducer(String supplier, String suffix, Properties properties, boolean testEnvironment) {
        super.configureProducer(supplier, suffix, properties, testEnvironment);

        String outputBinding = supplier + suffix;

        properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + outputBinding + ".producer.autobindDlq", "false");

    }
}
