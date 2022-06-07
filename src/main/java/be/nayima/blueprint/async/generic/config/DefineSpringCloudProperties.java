package be.nayima.blueprint.async.generic.config;

import be.nayima.blueprint.async.generic.processor.QueueFunctionDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

public abstract class DefineSpringCloudProperties {

    public static void setSpringCloudProperties(ConfigurableEnvironment environment,
                                                SpringApplication application,
                                                ArrayList<QueueFunctionDefinition> functionDefinitions,
                                                boolean testEnvironment) {
        Properties props = new Properties();

        for (var function : functionDefinitions) {
            function.configure(props, testEnvironment);
        }

        var dynFunctions = functionDefinitions.stream().map(QueueFunctionDefinition::processorName).filter(StringUtils::isNotEmpty).collect(Collectors.joining(";"));

        String functions = "batchJobProcessor" + ";" + dynFunctions;
        props.put("spring.cloud.stream.function.definition", functions);

        displayGeneratedProperties(props);

        environment.getPropertySources().addFirst(new PropertiesPropertySource("myProps", props));
    }

    private static void displayGeneratedProperties(Properties props) {
        var keys = props.keySet();
        var keysList = new ArrayList<String>(keys.size());
        for (var k : keys) {
            keysList.add((String) k);
        }
        Collections.sort(keysList);
        for (var key : keysList) {
            var value = props.getProperty(key);
            System.out.println(key + "=" + value);
        }
    }
}
