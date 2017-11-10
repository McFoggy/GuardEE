/**
 * Copyright © 2017 Matthieu Brouillard [http://oss.brouillard.fr/portable-mpft-jee] (matthieu@brouillard.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.brouillard.oss.ee.fault.tolerance.circuit_breaker;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;

import fr.brouillard.oss.ee.fault.tolerance.config.Globals;
import fr.brouillard.oss.ee.fault.tolerance.misc.Exceptions;

public class CircuitBreakerHandlerImpl implements CircuitBreakerHandler {

    private final Class<? extends Throwable>[] failOn;
    private final long windowDuration;
    private final double failureRatio;
    private final int successThreshold;
    private final ReentrantReadWriteLock rwLock;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;
    private final int volumeThreshold;
    private final boolean skipFirstSuccessForRatioComputation;
    private CircuitState state = CircuitState.CLOSED;
    private LimitedQueue<Execution> calls;

    public CircuitBreakerHandlerImpl(CircuitBreaker circuitBreaker) {
        this(
                circuitBreaker.failOn(),
                Duration.of(circuitBreaker.delay(), circuitBreaker.delayUnit()).toNanos(),
                circuitBreaker.requestVolumeThreshold(),
                circuitBreaker.failureRatio(),
                circuitBreaker.successThreshold()
        );
    }

    CircuitBreakerHandlerImpl(Class<? extends Throwable>[] failOn, long windowDurationInNanos, int volumeThreshold, double failureRatio, int successThreshold) {
        this.failOn = failOn;
        this.volumeThreshold = volumeThreshold;
        this.calls = new LimitedQueue<>(volumeThreshold);
        this.windowDuration = windowDurationInNanos;
        this.failureRatio = failureRatio;
        this.successThreshold = successThreshold;

        this.rwLock = new ReentrantReadWriteLock(true);
        this.readLock = rwLock.readLock();
        this.writeLock = rwLock.writeLock();

        this.skipFirstSuccessForRatioComputation = !Boolean.getBoolean(Globals.FT_CIRCUIT_BREAKER_FAILURE_RATIO_STRICT);
    }

    @Override
    public void enter() {
        long now = System.nanoTime();
        long lastFailure = lastFailure();

        if (CircuitState.OPENED == state && (lastFailure < now - windowDuration)) {
            setState(CircuitState.SEMI_OPENED);
        }

        if (CircuitState.OPENED == state) {
            throw new CircuitBreakerOpenException();
        }
    }

    void enter(int id) {
        try{
            System.out.println(String.format("[%d] before::enter - %s", id, state));
            enter();
        } finally {
            System.out.println(String.format("[%d] after::enter - %s", id, state));
        }
    }

    private void setState(CircuitState newState) {
        this.state = newState;
    }

    void success(int id) {
        try{
            System.out.println(String.format("[%d] before::success - %s", id, state));
            success();
        } finally {
            System.out.println(String.format("[%d] after::success - %s", id, state));
        }
    }
    @Override
    public void success() {
        try {
            if (writeLock.tryLock(2, TimeUnit.SECONDS)) {
                mark(false);
                if (CircuitState.SEMI_OPENED == state) {
                    long lastFailure = lastFailure();
                    int success = consecutiveSuccessAfterFailure(lastFailure);
                    if (success >= successThreshold) {
                        setState(CircuitState.CLOSED);
                    }
                }
            } else {
                throw new RuntimeException("cannot acquire lock to mark execution");
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException("interrupted while acquiring lock to mark execution");
        } finally {
            writeLock.unlock();
        }
    }

    private int consecutiveSuccessAfterFailure(long lastFailure) {
        return (int)calls.stream().filter(e -> e.isSuccess() && e.getTime() >= lastFailure).count();
    }

    private long lastFailure() {
        return calls.stream().filter(e -> !e.isSuccess()).max((o1, o2)->Long.compare(o1.getTime(), o2.getTime())).map(Execution::getTime).orElse(0l);
    }

    private void mark(boolean failure) {
        Long currentTime = System.nanoTime();
        calls.add(new Execution(currentTime, !failure));
    }

    public Throwable onFailure(Throwable t, int id) {
        try{
            System.out.println(String.format("[%d] before::failure - %s", id, state));
            boolean failure = Exceptions.isAssignableToAnyOf(failOn, t);
            mark(failure);

                if (failure) {
                    if (CircuitState.SEMI_OPENED == state) {
                        setState(CircuitState.OPENED);
                    } else {
                        double calculatedFailureRation = ratio(id);
                        if (calculatedFailureRation >= failureRatio) {
                            setState(CircuitState.OPENED);
                        }
                    }
            }

            return t;
        } finally {
            System.out.println(String.format("[%d] after::failure - %s", id, state));
        }
    }

    @Override
    public Exception onFailure(Exception t) {
        boolean failure = Exceptions.isAssignableToAnyOf(failOn, t);
        mark(failure);

        if (failure) {
            if (CircuitState.SEMI_OPENED == state) {
                setState(CircuitState.OPENED);
            } else {
                double calculatedFailureRation = ratio();
                if (calculatedFailureRation >= failureRatio) {
                    setState(CircuitState.OPENED);
                }
            }
        }

        return t;
    }

    /**
     * Exposed for tests only, return the current circuit breaker state
     * @return the current state
     */
    public CircuitState getState() {
        return state;
    }

    /**
     * Exposed for tests only, return the current circuit executions queue as a collection
     * @return the registered executions as a non null collection
     */
    Collection<Execution> getExecutions() {
        return new ArrayList<>(calls);
    }

    private double ratio() {
        long now = System.nanoTime();
        try {
            if (readLock.tryLock(2, TimeUnit.SECONDS)) {
                long windowStart = now - windowDuration;

                double callsInWindow = getCallsCount(this.calls, windowStart, skipFirstSuccessForRatioComputation);
                double failuresInWindow = this.calls.stream().filter(e -> !e.isSuccess() && e.getTime() > windowStart).count();

                if (callsInWindow >= volumeThreshold) {
                    return failuresInWindow / callsInWindow;
                } else {
                    return 0.0d;
                }
            };
        } catch (InterruptedException ex) {
            throw new RuntimeException("cannot acquire lock to mark execution");
        } finally {
            readLock.unlock();
        }

        return 1.0d;
    }

    private long getCallsCount(Collection<Execution> calls, long startOfWindow, boolean skipFirstSucess) {
        List<Execution> consideredCalls = calls
                .stream()
                .filter(e -> e.getTime() > startOfWindow)
                .collect(Collectors.toCollection(() -> new ArrayList<>(calls.size())));
        
        if (skipFirstSucess) {
            int firstFailureIndex = 0;
            for (int i = 0; i < consideredCalls.size(); i++) {
                if (!consideredCalls.get(i).isSuccess()) {
                    firstFailureIndex = i;
                    break;
                }
            }
            if (firstFailureIndex < consideredCalls.size()) {
                consideredCalls = consideredCalls.subList(firstFailureIndex, consideredCalls.size());
            }
        }
        
        return consideredCalls.stream().filter(e -> e.getTime() > startOfWindow).count();
    }

    private double ratio(int id) {
        long now = System.nanoTime();
        try {
            if (readLock.tryLock(2, TimeUnit.SECONDS)) {
                long windowStart = now - windowDuration;

                double callsInWindow = getCallsCount(calls, windowStart, skipFirstSuccessForRatioComputation);
                double failuresInWindow = calls.stream().filter(e -> !e.isSuccess() && e.getTime() > windowStart).count();

                if (callsInWindow >= volumeThreshold) {
                    return failuresInWindow / callsInWindow;
                } else {
                    return 0.0d;
                }
            };
        } catch (InterruptedException ex) {
            throw new RuntimeException("cannot acquire lock to mark execution");
        } finally {
            readLock.unlock();
        }

        return 1.0d;
    }

    /* visibility set to package for tests only */
    static class Execution {
        private final long time;
        private final boolean success;

        Execution(long time, boolean success) {

            this.time = time;
            this.success = success;
        }
        public long getTime() {
            return time;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
