package be.nayima.blueprint.async.asyncblueprint.usecase;

import be.nayima.blueprint.async.asyncblueprint.message.BasicJob;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CreateBasicJob {

    public BasicJob create() {
        return BasicJob.builder().body(UUID.randomUUID().toString()).build();
    }
}
