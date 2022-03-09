package be.nayima.blueprint.async.basicjob.connector;

import be.nayima.blueprint.async.basicjob.scheduler.SchedulingConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExternalParty implements IExternalParty {
    static int calls = 0;
    private final SchedulingConfig config;
    private final CircuitBreaker breaker;

    @Override
    public String call(String request) {
        return breaker.executeSupplier(() -> performCall(request));
    }

    @Override
    public int callsMade() {
        return calls;
    }

    private String performCall(String request) {
        calls++;
        log.info("Calling ExternalParty #{} with {}. Waiting for response", calls, request);
        if (calls % 2 == 0) {
            log.error("Oh noes! Something went wrong with the external party!");
            throw new RuntimeException("ExternalParty is down");
        }
        try {
            Thread.sleep(config.getBasicJobProcessingInterval() * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("We finally got an answer...");
        return request;
    }
}
