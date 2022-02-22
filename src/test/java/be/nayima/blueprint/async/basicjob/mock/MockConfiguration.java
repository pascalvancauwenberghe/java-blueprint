package be.nayima.blueprint.async.basicjob.mock;

import be.nayima.blueprint.async.basicjob.connector.IExternalParty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class MockConfiguration {
    private final MockExternalParty externalParty = new MockExternalParty();

    @Bean
    @Primary
    public IExternalParty makeExternalParty() {
        return externalParty;
    }

    @Bean
    public MockExternalParty mockExternalParty() {
        return externalParty;
    }
}
