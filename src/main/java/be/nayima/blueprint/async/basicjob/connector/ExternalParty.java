package be.nayima.blueprint.async.basicjob.connector;

import be.nayima.blueprint.async.basicjob.scheduler.SchedulingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ExternalParty implements IExternalParty {
    private final SchedulingConfig config;

    @Override
    public String call(String request) {
        log.info("Calling ExternalParty with {}. Waiting for response", request);
        try {
            Thread.sleep(config.getBasicJobProcessingInterval() * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("We finally got an answer...");
        return request;
    }
}
