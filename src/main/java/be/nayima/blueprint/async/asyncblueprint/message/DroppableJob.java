package be.nayima.blueprint.async.asyncblueprint.message;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Data
@SuperBuilder
@Jacksonized
public class DroppableJob {
    public Instant ttl;
    public String name;
}
