package be.nayima.blueprint.async.basicjob.mock;

import be.nayima.blueprint.async.basicjob.connector.IExternalParty;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.fallback.autoconfigure.FallbackConfigurationOnMissingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@TestConfiguration
@Import(FallbackConfigurationOnMissingBean.class)
public class MockConfiguration {
    private final MockExternalParty externalParty = new MockExternalParty();

    @Bean
    @Primary
    public IExternalParty makeExternalParty() {
        return externalParty;
    }

    @Bean
    public MockExternalParty mockExternalParty() {
        return externalParty;
    }


    @Autowired
    public CircuitBreakerRegistry circuitBreakerRegistry;
}
