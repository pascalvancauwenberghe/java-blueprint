package be.nayima.blueprint.async.persistent.usecase;

import be.nayima.blueprint.async.persistent.connector.CallFailedException;

public interface FailingPartyCaller {
    void call(String request, int counter) throws CallFailedException;
}
