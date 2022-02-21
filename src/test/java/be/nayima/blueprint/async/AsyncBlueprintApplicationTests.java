package be.nayima.blueprint.async;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({TestChannelBinderConfiguration.class})
@ActiveProfiles({"test"})
class AsyncBlueprintApplicationTests {

    @Test
    void contextLoads() {
        Assert.assertTrue("Check Spring dynamic loading works", true);
    }


}
