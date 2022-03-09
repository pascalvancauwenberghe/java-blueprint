package be.nayima.blueprint.async.basicjob.integration;

import be.nayima.blueprint.async.basicjob.connector.ExternalParty;
import be.nayima.blueprint.async.basicjob.mock.MockConfiguration;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import({TestChannelBinderConfiguration.class, MockConfiguration.class})
@ActiveProfiles({"test"})
public class CircuitBreakerIntegrationTest {

    @Autowired
    ExternalParty externalParty;
    @Autowired
    CircuitBreaker circuitBreaker;

    @Test
    public void externalPartyHasCircuitBreaker() {
        final int CALLS = 100;
        int circuitOpen = 0;
        int succeeded = 0;
        int failed = 0;
        for (int i = 0; i < CALLS; i++) {

            try {
                externalParty.call("hello");
                succeeded++;
            } catch (CallNotPermittedException circuitbreakerOpen) {
                circuitOpen++;
            } catch (Exception ex) {
                failed++;
            }
        }
        assertThat(succeeded + failed + circuitOpen).isEqualTo(CALLS);
        assertThat(circuitOpen).isGreaterThan(0);
    }

    @Test
    public void hasCircuitBreakerForExternalParty() {
        Assert.assertNotNull(circuitBreaker);
        Assert.assertEquals("ExternalParty", circuitBreaker.getName());
        Assert.assertEquals(30, circuitBreaker.getCircuitBreakerConfig().getSlidingWindowSize());
        var result = circuitBreaker.executeSupplier(() -> {
            return hopla();
        });
        Assert.assertEquals("Hopla", result);
    }

    private String hopla() {
        return "Hopla";
    }

    @Test
    public void pickingUpConfiguration() {
        var config = circuitBreaker.getCircuitBreakerConfig() ;
        // Must match settings from application.yml
        assertThat(config.getSlidingWindowSize()).isEqualTo(30) ;
        assertThat(config.getSlidingWindowType()).isEqualTo(CircuitBreakerConfig.SlidingWindowType.TIME_BASED) ;
        assertThat(config.getMinimumNumberOfCalls()).isEqualTo(5) ;
        assertThat(config.getFailureRateThreshold()).isEqualTo(10.0f) ;
    }
}
