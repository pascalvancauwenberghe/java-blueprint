package be.nayima.blueprint.async.generic.processor;

import java.util.Properties;

public class TransientQueueDefinition extends QueueDefinition {
    public TransientQueueDefinition(String exchange, String queue) {
        super(exchange, queue);
    }

    public void configureConsumer(String processor, String suffix, Properties properties) {
        super.configureConsumer(processor, suffix, properties);

        String inputBinding = processor + suffix;

        properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + inputBinding + ".consumer.autobindDlq", "false");
    }

    public void configureProducer(String supplier, String suffix, Properties properties) {
        super.configureProducer(supplier, suffix, properties);

        String outputBinding = supplier + suffix;

        properties.put(SPRING_CLOUD_STREAM_RABBIT_BINDINGS + outputBinding + ".producer.autobindDlq", "false");

    }
}
