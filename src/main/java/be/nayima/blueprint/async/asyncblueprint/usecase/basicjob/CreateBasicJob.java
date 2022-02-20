package be.nayima.blueprint.async.asyncblueprint.usecase.basicjob;

import be.nayima.blueprint.async.asyncblueprint.message.basicjob.BasicJob;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class CreateBasicJob {

    // This creates new jobs by performing some business logic to determine which job(s) need to be done
    // Just create some random BasicJob
    public BasicJob create() {
        return BasicJob.builder().body(UUID.randomUUID().toString()).build();
    }

    public List<BasicJob> createBatch() {
        var jobs = new ArrayList<BasicJob>();

        int batchSize = ThreadLocalRandom.current().nextInt(5, 10);

        for (int i = 0; i < batchSize; i++) {
            jobs.add(create());
        }
        return jobs;
    }
}
