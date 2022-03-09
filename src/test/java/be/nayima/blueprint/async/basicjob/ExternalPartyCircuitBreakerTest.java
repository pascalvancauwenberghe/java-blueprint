package be.nayima.blueprint.async.basicjob;

import be.nayima.blueprint.async.basicjob.connector.ExternalParty;
import be.nayima.blueprint.async.basicjob.mock.CircuitBreakerMock;
import be.nayima.blueprint.async.basicjob.scheduler.SchedulingConfig;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ExternalPartyCircuitBreakerTest {
    @Test
    public void breakit() {
        var config = new SchedulingConfig();
        var breaker = new CircuitBreakerMock("MockExternalParty");
        var externalParty = new ExternalParty(config, breaker);

        breaker.transitionToClosedState();

        externalParty.call("1");
        try {
            externalParty.call("2");
            Assert.fail("Second call should fail");
        } catch (Exception expected) {

        }
        assertThat(externalParty.callsMade()).isEqualTo(2);

        breaker.transitionToOpenState();
        try {
            externalParty.call("3");
            Assert.fail("Should not make call when circuitbreaker Open");
        } catch (CallNotPermittedException expected) {

        }
        assertThat(externalParty.callsMade()).isEqualTo(2);
    }
}
