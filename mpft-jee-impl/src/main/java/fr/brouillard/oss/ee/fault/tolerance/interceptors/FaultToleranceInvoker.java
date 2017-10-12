package fr.brouillard.oss.ee.fault.tolerance.interceptors;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;

import fr.brouillard.oss.ee.fault.tolerance.model.RetryConfiguration;

public class FaultToleranceInvoker {
    private Random randomizer;

    @PostConstruct
    void initialize() {
        randomizer = new Random(System.nanoTime());
    }

    public Function<InvocationContext, Object> retry(RetryConfiguration retryCfg) {
        return ic -> {
            boolean ended = true;
            int retry = 0;
            Throwable latestFailure = null;

            Instant start = Clock.systemDefaultZone().instant();

            // TODO maxDuration

            long delayInMillis = computeDelayInMillis(retryCfg, start);
            long jitterInMillis = 0;

            do {
                if (retry > 0) {
                    jitterInMillis = computeJitterInMillis(retryCfg, start);
                    try {
                        Thread.sleep(delayInMillis+jitterInMillis);
                    } catch (InterruptedException e) {}
                }
                try {
                    return ic.proceed();
                } catch (Throwable t) {
                    latestFailure = t;
                    if (isAssignableToAnyOf(retryCfg.getAbortOn(), t) && !isAssignableToAnyOf(retryCfg.getRetryOn(), t)) {
                        break;
                    }
                }

                ended = (++retry < retryCfg.getMaxRetries());
            } while (!ended);

            throw new FaultToleranceException(latestFailure);
        };
    }

    private boolean isAssignableToAnyOf(Class<? extends Throwable>[] abortOn, Throwable t) {
        return Arrays.asList(abortOn).stream().filter(c -> c.isInstance(t)).findFirst().isPresent();
    }

    private long computeJitterInMillis(RetryConfiguration retryCfg, Instant baseInstant) {
        return Duration.between(baseInstant, retryCfg.getJitterDelayUnit().addTo(baseInstant, randomizer.nextInt((int)retryCfg.getJitter()))).toMillis();
    }

    private long computeDelayInMillis(RetryConfiguration retryCfg, Instant baseInstant) {
        return Duration.between(baseInstant, retryCfg.getDelayUnit().addTo(baseInstant, retryCfg.getDelay())).toMillis();
    }
}
