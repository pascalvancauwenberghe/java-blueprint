package be.nayima.blueprint.async.basicjob.integration;

import be.nayima.blueprint.async.basicjob.connector.ExternalParty;
import be.nayima.blueprint.async.basicjob.mock.MockConfiguration;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({TestChannelBinderConfiguration.class, MockConfiguration.class})
@ActiveProfiles({"test"})
public class CircuitBreakerIntegrationTest {
    @Autowired
    ExternalParty externalParty;
    @Autowired
    MockConfiguration config;

    @Test
    public void hasCircuitBreakerAnnotation() {
        int circuitOpen = 0;
        int succeeded = 0;
        int failed = 0;
        for (int i = 0; i < 100; i++) {

            try {
                externalParty.call("hello");
                succeeded++;
            } catch (CallNotPermittedException circuitbreakerOpen) {
                circuitOpen++;
            } catch (Exception ex) {
                failed++;
            }
        }
        Assert.assertEquals(100, succeeded + failed + circuitOpen);
        Assert.assertTrue(circuitOpen > 0);
    }

    @Test
    public void hasCircuitBreakerForExternalParty() {
        Assert.assertNotNull(config.circuitBreakerRegistry);
        var breaker = config.circuitBreakerRegistry.circuitBreaker("ExternalParty");
        Assert.assertNotNull(breaker) ;
        Assert.assertEquals(30,breaker.getCircuitBreakerConfig().getSlidingWindowSize());
        breaker.executeRunnable(() -> { hopla();});
    }

    private void hopla() {
        System.out.println("Hopla");
    }
}
