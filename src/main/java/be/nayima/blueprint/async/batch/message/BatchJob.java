package be.nayima.blueprint.async.batch.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class BatchJob {
    private final OffsetDateTime createdOn;
    private final int counter;
}
