package be.nayima.blueprint.async.batch.message;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter

public class BatchResult {
    private final OffsetDateTime createdOn;
    private final int count;

    public BatchResult(int count) {
        this.createdOn = OffsetDateTime.now();
        this.count = count;
    }
}
