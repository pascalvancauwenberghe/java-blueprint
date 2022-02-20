package be.nayima.blueprint.async.asyncblueprint;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Import({ TestChannelBinderConfiguration.class })
@ActiveProfiles({ "test" })
class AsyncBlueprintApplicationTests {

	@Test
	void contextLoads() {
	}

}
