package be.nayima.blueprint.async.basicjob;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

interface RemoteService {
    int process(int i);
}

@Data
class Service implements RemoteService {

    private int counter;

    @Override
    public int process(int i) {
        counter++;
        throw new RuntimeException("Help!");
    }
}

public class CircuitBreakerTest {
    @Test
    public void breakit() {

        int circuitBreakerOpenCalls = 0;
        final int[] notPermitted = {0};
        var service = new Service();
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .minimumNumberOfCalls(4)
                .failureRateThreshold(50)
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker circuitBreaker = registry.circuitBreaker("ExternalParty");
        circuitBreaker.getEventPublisher().onCallNotPermitted(event -> {
            notPermitted[0]++;
        });
        Function<Integer, Integer> decorated = CircuitBreaker
                .decorateFunction(circuitBreaker, service::process);

        for (int i = 0; i < 10; i++) {
            try {
                decorated.apply(i);
            } catch (CallNotPermittedException circuitbreakerOpen) {
                circuitBreakerOpenCalls++;
            } catch (Exception ignore) {
            }
        }

        Assertions.assertEquals(4, service.getCounter());
        Assertions.assertEquals(6, circuitBreakerOpenCalls);
        Assertions.assertEquals(6, notPermitted[0]);
    }
}
