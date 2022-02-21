package be.nayima.blueprint.async.basicjob.message;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class BasicJob {
    public String body;
}
