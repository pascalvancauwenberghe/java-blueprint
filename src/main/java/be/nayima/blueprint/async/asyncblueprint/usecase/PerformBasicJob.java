package be.nayima.blueprint.async.asyncblueprint.usecase;

import be.nayima.blueprint.async.asyncblueprint.config.SchedulingConfig;
import be.nayima.blueprint.async.asyncblueprint.message.BasicJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PerformBasicJob implements IPerformDroppableWork<BasicJob> {
    private final SchedulingConfig config;

    @Override
    public void perform(BasicJob in) {

        log.info("Done with {}. Feeling sleepy after all that work... Sleeping {} seconds", in.getBody(), config.getBasicJobProcessingInterval());
        try {
            Thread.sleep(config.getBasicJobProcessingInterval() * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Waking up refreshed...");
    }

    @Override
    public void drop(BasicJob in) {

    }
}
