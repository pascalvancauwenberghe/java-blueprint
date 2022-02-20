package be.nayima.blueprint.async.asyncblueprint.processor;

import be.nayima.blueprint.async.asyncblueprint.message.BasicJob;
import be.nayima.blueprint.async.asyncblueprint.usecase.PerformBasicJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class BasicJobExecutor {

    private final PerformBasicJob performer;

    public void process(BasicJob in) {
        var now = Instant.now();
        var expired = now.isAfter(in.getTtl());

        if (expired) {
            performer.drop(in);
        } else {
            performer.perform(in);
        }
    }

}
