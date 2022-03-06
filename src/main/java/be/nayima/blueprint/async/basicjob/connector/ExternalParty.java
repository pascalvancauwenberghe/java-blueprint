package be.nayima.blueprint.async.basicjob.connector;

import be.nayima.blueprint.async.basicjob.scheduler.SchedulingConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExternalParty implements IExternalParty {
    private final SchedulingConfig config;

    @Override
    @CircuitBreaker(name = "ExternalParty")
    public String call(String request) {
        log.info("Calling ExternalParty with {}. Waiting for response", request);
        var rnd = ThreadLocalRandom.current().nextInt(1, 10);
        if (rnd > 3) {
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
