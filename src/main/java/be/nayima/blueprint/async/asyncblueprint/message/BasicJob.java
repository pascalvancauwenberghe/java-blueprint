package be.nayima.blueprint.async.asyncblueprint.message;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class BasicJob {
    public String body;
}
