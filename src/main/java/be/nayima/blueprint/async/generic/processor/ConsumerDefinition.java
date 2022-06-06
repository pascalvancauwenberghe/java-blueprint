package be.nayima.blueprint.async.generic.processor;

import java.util.Properties;

public class ConsumerDefinition implements QueueFunctionDefinition {
    private final String name;
    private final QueueDefinition queueDefinition;
    private final String suffix;

    public ConsumerDefinition(String name, QueueDefinition queueDefinition) {
        this.name = name;
        this.queueDefinition = queueDefinition;
        this.suffix = "-in-0";
    }

    public void configure(Properties properties) {
        queueDefinition.configureConsumer(name, suffix, properties);
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
