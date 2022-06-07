package be.nayima.blueprint.async.batch.processor;

import be.nayima.blueprint.async.batch.message.BatchJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchJobSupplier {
    private static int counter = 1;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.from(ZoneOffset.UTC));

    // StreamBridge should be an interface instead of implementation, so that we can mock it in unit tests
    private final StreamBridge streamBridge;

    public void supplyJob() {
        var id = counter;
        counter++;
        var batchJob = new BatchJob(OffsetDateTime.now(), counter);
        sendBatchJob(batchJob);
    }

    private void sendBatchJob(BatchJob job) {

        String bindingName = BatchStreamConfig.batchJobProducer.bindingName();
        log.info("SEND message in Batch to {}. Message {} created at {}", bindingName, job.getCounter(), job.getCreatedOn());
        streamBridge.send(bindingName, job);
    }

}
