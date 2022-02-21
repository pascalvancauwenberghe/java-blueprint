package be.nayima.blueprint.async.basicjob;

import be.nayima.blueprint.async.basicjob.processor.BasicJobSupplier;
import be.nayima.blueprint.async.basicjob.usecase.PerformBasicJob;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;

@SpringBootTest
@Import({TestChannelBinderConfiguration.class})
@ActiveProfiles({"test"})
class BasicJobApplicationTests {

    @Autowired
    BasicJobSupplier supplier;
    @Autowired
    PerformBasicJob performer;

    @Test
    void InjectBasicJob() {
        final int numberOfMessagesSent = 4;
        for (int i = 0; i < numberOfMessagesSent; i++) {
            supplier.supplyJob(Instant.now().plus(Duration.ofSeconds(5)));
        }

        Assert.assertEquals(numberOfMessagesSent, performer.getMessages());
        Assert.assertEquals(numberOfMessagesSent, performer.getMessagesPerformed() + performer.getMessagesDropped());

    }

}
