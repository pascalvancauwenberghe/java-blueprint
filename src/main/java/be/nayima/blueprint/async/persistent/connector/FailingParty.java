package be.nayima.blueprint.async.persistent.connector;

public interface FailingParty {
    String call(String request, int counter) throws CallFailedException;
}
