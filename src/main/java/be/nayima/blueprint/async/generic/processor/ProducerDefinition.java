package be.nayima.blueprint.async.generic.processor;

import java.util.Properties;

public class ProducerDefinition implements QueueFunctionDefinition {
    private final String name;
    private final QueueDefinition queueDefinition;
    private final String suffix;

    public ProducerDefinition(String name, QueueDefinition queueDefinition) {
        this.name = name;
        this.queueDefinition = queueDefinition;
        this.suffix = "-out-0";
    }

    public ProducerDefinition(String name, QueueDefinition queueDefinition, String suffix) {
        this.name = name;
        this.queueDefinition = queueDefinition;
        this.suffix = "-" + suffix;
    }

    public void configure(Properties properties) {
        queueDefinition.configureProducer(name, suffix, properties);
    }

    @Override
    public String processorName() {
        return null;
    }

    @Override
    public String bindingName() {
        return name + suffix;
    }
}
