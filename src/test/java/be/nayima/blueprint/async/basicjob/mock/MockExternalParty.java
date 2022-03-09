package be.nayima.blueprint.async.basicjob.mock;

import be.nayima.blueprint.async.basicjob.connector.IExternalParty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockExternalParty implements IExternalParty {
    @Getter
    private int messages = 0;

    @Override
    public String call(String request) {
        log.info("Mocking External Party");

        messages++;
        return request;
    }

    @Override
    public int callsMade() {
        return messages;
    }
}
