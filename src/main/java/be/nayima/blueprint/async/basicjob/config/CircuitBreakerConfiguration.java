package be.nayima.blueprint.async.basicjob.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class CircuitBreakerConfiguration {
    public static final String CIRCUIT_BREAKER_NAME = "ExternalParty";

    @Autowired
    private final CircuitBreakerRegistry registry;

    @Bean(name = CIRCUIT_BREAKER_NAME)
    public CircuitBreaker ipexCircuitBreaker() {
        var breaker = registry.circuitBreaker(CIRCUIT_BREAKER_NAME);
        return breaker;
    }
}
