package be.nayima.blueprint.async.generic.message;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Data
@Builder
@Jacksonized
public class DroppableJob<Body> {
    public Instant expiresAt;
    public String name;
    public Body body;
}
