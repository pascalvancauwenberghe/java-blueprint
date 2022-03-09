package be.nayima.blueprint.async.basicjob.mock;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.vavr.collection.Map;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

// Enkel de gebruikte methodes zijn geïmplementeerd
public class CircuitBreakerMock implements CircuitBreaker {

    private CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();
    private String name;
    private State state = State.CLOSED;

    public CircuitBreakerMock(String name) {
        this.name = name;
    }

    @Override
    public void executeRunnable(Runnable runnable) {
        decorateRunnable(runnable).run();
    }

    @Override
    public Runnable decorateRunnable(Runnable runnable) {
        final CircuitBreaker me = this;
        return new Runnable() {
            @Override
            public void run() {
                if (state == State.CLOSED) {
                    runnable.run();
                } else {
                    throw CallNotPermittedException.createCallNotPermittedException(me);
                }
            }
        };
    }

    public <T> T executeSupplier(Supplier<T> supplier) {
        return decorateSupplier(supplier).get();
    }

    @Override
    public <T> Supplier<T> decorateSupplier(Supplier<T> supplier) {
        return () -> {
            if (state == State.CLOSED) {
                return supplier.get();
            } else {
                throw CallNotPermittedException.createCallNotPermittedException(this);
            }
        };
    }

    public <T> void executeConsumer(Consumer<T> consumer, T value) {
        decorateConsumer(consumer).accept(value);
    }

    @Override
    public <T> Consumer<T> decorateConsumer(Consumer<T> consumer) {
        return (t) -> {
            if (state == State.CLOSED) {
                consumer.accept(t);
            } else {
                throw CallNotPermittedException.createCallNotPermittedException(this);
            }
        };
    }

    @Override
    public boolean tryAcquirePermission() {
        return true;
    }

    @Override
    public void releasePermission() {

    }

    @Override
    public void acquirePermission() {

    }

    @Override
    public void onError(long duration, TimeUnit durationUnit, Throwable throwable) {

    }

    @Override
    public void onSuccess(long duration, TimeUnit durationUnit) {

    }

    @Override
    public void onResult(long duration, TimeUnit durationUnit, Object result) {

    }

    @Override
    public void reset() {

    }

    @Override
    public void transitionToClosedState() {
        state = State.CLOSED;
    }

    @Override
    public void transitionToOpenState() {
        state = State.OPEN;
    }

    @Override
    public void transitionToHalfOpenState() {
        state = State.HALF_OPEN;
    }

    @Override
    public void transitionToDisabledState() {
        state = State.DISABLED;
    }

    @Override
    public void transitionToMetricsOnlyState() {

    }

    @Override
    public void transitionToForcedOpenState() {
        state = State.FORCED_OPEN;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public CircuitBreakerConfig getCircuitBreakerConfig() {
        return config;
    }

    @Override
    public Metrics getMetrics() {
        return null;
    }

    @Override
    public Map<String, String> getTags() {
        return null;
    }

    @Override
    public EventPublisher getEventPublisher() {
        return null;
    }

    @Override
    public long getCurrentTimestamp() {
        return 0;
    }

    @Override
    public TimeUnit getTimestampUnit() {
        return null;
    }
}
