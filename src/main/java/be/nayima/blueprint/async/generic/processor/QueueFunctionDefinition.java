package be.nayima.blueprint.async.generic.processor;

import java.util.Properties;

public interface QueueFunctionDefinition {
    void configure(Properties properties);

    String processorName();

    String bindingName() ;
}
