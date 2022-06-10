package be.nayima.blueprint.async.config;

import be.nayima.blueprint.async.generic.config.DefineSpringCloudProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

@Order(Ordered.LOWEST_PRECEDENCE)
@Slf4j
public class TestDynamicProperties implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        DefineSpringCloudProperties.setSpringCloudProperties(environment, application, AsyncConfigurations.gatherConfigurations(), true);
    }
}
