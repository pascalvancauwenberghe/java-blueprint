package be.nayima.blueprint.async.persistent.message;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Data
@Builder
@Jacksonized
public class PersistentJob {
    public String body;
    public int counter;
    public boolean persistent;
    public Instant expiresAt;
}
