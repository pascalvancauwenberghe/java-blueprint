package be.nayima.blueprint.async.generic.processor;

import be.nayima.blueprint.async.generic.message.DroppableJob;
import be.nayima.blueprint.async.generic.usecase.IPerformDroppableWork;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
// Generic processor which is fed from a queue of DroppableJob<Job>
// The processor uses the DroppableJob envelope to determine if the job has exceeded its TTL in the queue
// The IPerformDroppableWork<Job> implementation handles both "fresh" (perform) and "stale" (drop) jobs.
public class DroppableJobExecutor<Job, Performer extends IPerformDroppableWork<Job>> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
    private final Performer performer;

    public void process(DroppableJob<Job> in) {
        var now = Instant.now();
        var expired = now.isAfter(in.getExpiresAt());

        if (expired) {
            // Dropping droppable jobs is not an error, but could be an indication of insufficient processor power if it happens regularly
            log.warn("DROP. Message {} should have been processed before {}.", in.getName(), formatter.format(in.getExpiresAt()));
            performer.drop(in.getBody());
        } else {
            log.info("PROC. Message {}               is processed before {}.", in.getName(), formatter.format(in.getExpiresAt()));
            performer.perform(in.getBody());
        }
    }

}
