package be.nayima.blueprint.async.persistent.processor;

import be.nayima.blueprint.async.persistent.connector.CallFailedException;
import be.nayima.blueprint.async.persistent.message.PersistentJob;
import be.nayima.blueprint.async.persistent.usecase.FailingPartyCaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
// Generic processor which is fed from a queue of DroppableJob<Job>
// The processor uses the DroppableJob envelope to determine if the job has exceeded its TTL in the queue
// The IPerformDroppableWork<Job> implementation handles both "fresh" (perform) and "stale" (drop) jobs.
public class PersistentJobProcessor {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
    private final FailingPartyCaller usecase;
    private static int lastReceived = 0;
    private static final int MAX_RESURRECTIONS = 3;

    public void process(Message<PersistentJob> msg) {
        var in = msg.getPayload();
        if (!in.isPersistent() && in.getExpiresAt().isAfter(Instant.now())) {
            log.info("Dropping transient expired message #{}", in.getCounter());
            return;
        }

        try {
            usecase.call(in.getBody(), in.getCounter());
            log.info("DONE. Message {} processing at {}. {}.", in.getCounter(), Instant.now(), ordered(in));
            lastReceived = in.getCounter();
        } catch (CallFailedException e) {
            var headers = msg.getHeaders();
            var resurrections = resurrections(headers);
            log.info("FAIL. Message {} processing at {}. {}. Resurrected {} times", in.getCounter(), Instant.now(), ordered(in), resurrections);

            if (resurrections > MAX_RESURRECTIONS) {
                log.error("Maximum resurrections reached for message #{}. Leaving it in peace", in.getCounter());
            } else {
                throw new RuntimeException(e);
            }
        }

    }

    private String ordered(PersistentJob in) {
        return in.getCounter() < lastReceived ? "Out of order" : "In order";
    }

    private int resurrections(MessageHeaders headers) {
        Object xDeath = headers.get("x-death");
        if (xDeath != null) {
            var deathList = (List) xDeath;
            if (deathList.size() > 0) {
                var deathMap = (Map) deathList.get(0);
                var count = deathMap.get("count");
                var countL = (Long) count;
                return countL.intValue();
            }
        }
        return 0;
    }
}


