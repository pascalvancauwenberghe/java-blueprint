package be.nayima.blueprint.async.persistent.connector;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class FailingPartyImpl implements FailingParty {
    private boolean fail = false;

    @Override
    public String call(String request, int counter) throws CallFailedException {
        int r = ThreadLocalRandom.current().nextInt(1, 10);
        if (counter % 10 == 1) {

            throw new CallFailedException(request);
        }
        return request;
    }

    public void setFail(boolean mustFail) {
        fail = mustFail;
    }
}
