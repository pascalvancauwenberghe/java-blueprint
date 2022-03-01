package be.nayima.blueprint.async.persistent.connector;

public class CallFailedException extends Exception {
    public CallFailedException(String reason) {
        super(reason);
    }
}
