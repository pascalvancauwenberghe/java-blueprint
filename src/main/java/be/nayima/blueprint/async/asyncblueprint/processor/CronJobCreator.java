package be.nayima.blueprint.async.asyncblueprint.processor;

import be.nayima.blueprint.async.asyncblueprint.config.SchedulingConfig;
import be.nayima.blueprint.async.asyncblueprint.message.BasicJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CronJobCreator {
    private static final String EXCHANGE_OUT = "Blueprint.Scheduled.BasicJobs";
    private static DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));

    private final SchedulingConfig config;
    private final StreamBridge streamBridge;

    @Scheduled(cron = "${blueprint.schedules.basic-job-creation-schedule}")
    public void generateJob() {
        var now = Instant.now();
        var ttl = now.plus(1, ChronoUnit.SECONDS);
        log.info("Sending message on schedule {} which expires at {}", config.getBasicJobCreationSchedule(), formatter.format(ttl));

        streamBridge.send(EXCHANGE_OUT, BasicJob.builder().ttl(ttl).name("BasicJob").body(UUID.randomUUID().toString()).build());
    }

}
