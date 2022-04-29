package be.nayima.blueprint.async.batch.message;

import lombok.Getter;

import java.time.OffsetDateTime;

@Getter

public class BatchJob {
    private final OffsetDateTime createdOn;
    private final int counter;

    public BatchJob(int counter) {
        this.createdOn = OffsetDateTime.now();
        this.counter = counter;
    }
}
