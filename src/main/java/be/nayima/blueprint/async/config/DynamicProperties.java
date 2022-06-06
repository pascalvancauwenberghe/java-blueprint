package be.nayima.blueprint.async.config;

import be.nayima.blueprint.async.basicjob.processor.BasicJobStreamConfig;
import be.nayima.blueprint.async.generic.processor.QueueFunctionDefinition;
import be.nayima.blueprint.async.persistent.processor.PersistentStreamConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.stream.Collectors;

@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class DynamicProperties implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        Properties props = new Properties();

        var functionDefinitions = new ArrayList<QueueFunctionDefinition>() ;
        functionDefinitions.addAll(BasicJobStreamConfig.allFunctions()) ;
        functionDefinitions.addAll(PersistentStreamConfig.allFunctions()) ;

        for (var function : functionDefinitions) {
            function.configure(props);
        }

        var dynFunctions = functionDefinitions.stream().map(QueueFunctionDefinition::processorName).filter(StringUtils::isNotEmpty).collect(Collectors.joining(";"));

        String functions = "batchJobProcessor" + ";" + dynFunctions;
        props.put("spring.cloud.stream.function.definition", functions);

        displayGeneratedProperties(props);

        environment.getPropertySources().addFirst(new PropertiesPropertySource("myProps", props));
    }

    private void displayGeneratedProperties(Properties props) {
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
