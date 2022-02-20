package be.nayima.blueprint.async.asyncblueprint.usecase;

import be.nayima.blueprint.async.asyncblueprint.message.BasicJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PerformBasicJob implements IPerformDroppableWork<BasicJob> {
    @Override
    public void perform(BasicJob in) {

        log.info("Feeling sleepy after all that work...");
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Waking up refreshed...");
    }

    @Override
    public void drop(BasicJob in) {

    }
}
