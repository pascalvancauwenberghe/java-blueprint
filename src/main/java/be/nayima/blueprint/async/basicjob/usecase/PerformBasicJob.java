package be.nayima.blueprint.async.basicjob.usecase;

import be.nayima.blueprint.async.basicjob.scheduler.SchedulingConfig;
import be.nayima.blueprint.async.basicjob.message.BasicJob;
import be.nayima.blueprint.async.generic.usecase.IPerformDroppableWork;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PerformBasicJob implements IPerformDroppableWork<BasicJob> {
    private final SchedulingConfig config;

    @Getter
    private int messages = 0;
    @Getter
    private int messagesDropped = 0;
    @Getter
    private int messagesPerformed = 0;

    // Perform the work of processing the BasicJob.
    // This should not throw exceptions
    // This may take some time
    @Override
    public void perform(BasicJob in) {

        // Do the work
        messages += 1;
        messagesPerformed += 1;
        log.info("Done with {}. Feeling sleepy after all that work... Sleeping {} seconds", in.getBody(), config.getBasicJobProcessingInterval());
        try {
            Thread.sleep(config.getBasicJobProcessingInterval() * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("Waking up refreshed...");
    }

    // Handle the case where the job's TTL has expired
    // This should not throw exceptions
    // This should be fast, because if there's a big backlog of 'stale', we want to clear it quickly to get to the 'fresh' work
    @Override
    public void drop(BasicJob in) {
        // Doing nothing is pretty fast
        messages += 1;
        messagesDropped += 1;
    }
}
