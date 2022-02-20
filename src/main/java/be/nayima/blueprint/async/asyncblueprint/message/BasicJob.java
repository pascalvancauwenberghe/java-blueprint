package be.nayima.blueprint.async.asyncblueprint.message;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Data
@SuperBuilder
@Jacksonized
@EqualsAndHashCode(callSuper=true)
public class BasicJob extends DroppableJob {
    public String body;
}
