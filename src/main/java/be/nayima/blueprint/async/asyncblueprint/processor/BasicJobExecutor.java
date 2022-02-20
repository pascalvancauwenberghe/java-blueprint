package be.nayima.blueprint.async.asyncblueprint.processor;

import be.nayima.blueprint.async.asyncblueprint.message.BasicJob;
import be.nayima.blueprint.async.asyncblueprint.message.DroppableJob;
import be.nayima.blueprint.async.asyncblueprint.usecase.PerformBasicJob;
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
public class BasicJobExecutor {
    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));
    private final PerformBasicJob performer;

    public void process(DroppableJob<BasicJob> in) {
        var now = Instant.now();
        var expired = now.isAfter(in.getTtl());

        if (expired) {
            log.warn("DROP. Message {} should have been processed before {}.", in.getName(), formatter.format(in.getTtl()));
            performer.drop(in.getBody());
        } else {
            log.info("PROC. Message {}               is processed before {}.", in.getName(), formatter.format(in.getTtl()));
            performer.perform(in.getBody());
        }
    }

}
