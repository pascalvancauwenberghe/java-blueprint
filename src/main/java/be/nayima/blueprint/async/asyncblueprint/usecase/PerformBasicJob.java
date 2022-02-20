package be.nayima.blueprint.async.asyncblueprint.usecase;


import be.nayima.blueprint.async.asyncblueprint.message.BasicJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class PerformBasicJob implements IPerformDroppableWork<BasicJob> {
    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));

    @Override
    public void perform(BasicJob in) {
        log.info("PROC. Message {}               is processed before {}.", in.getName(), formatter.format(in.getTtl()));
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
        log.warn("DROP. Message {} should have been processed before {}.", in.getName(), formatter.format(in.getTtl()));
    }
}
