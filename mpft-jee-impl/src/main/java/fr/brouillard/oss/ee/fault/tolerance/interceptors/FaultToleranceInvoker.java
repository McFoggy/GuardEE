package fr.brouillard.oss.ee.fault.tolerance.interceptors;

import fr.brouillard.oss.ee.fault.tolerance.model.InvocationConfiguration;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;

@ApplicationScoped
public class FaultToleranceInvoker {
    private Random rnd;

    @Inject
    TimeoutManager tm;

    public FaultToleranceInvoker() {
    }

    @PostConstruct
    public void initialize() {
        rnd = new Random(System.nanoTime());
    }

    public Object invoke(InvocationConfiguration cfg, InvocationContext context) {
            boolean ended = true;
            int retry = 0;
            Throwable latestFailure = null;

            Clock clock = Clock.systemUTC();
            Instant start = clock.instant();
            Instant until = cfg.getDurationUnit().addTo(start, cfg.getMaxDuration());

            long delayInMillis = computeDelayInMillis(cfg, start);
            long jitterInMillis = 0;

            do {
                if (retry > 0 && cfg.getMaxRetries() > 0) {
                    jitterInMillis = computeJitterInMillis(cfg, start);
                    try {
                        Thread.sleep(delayInMillis+jitterInMillis);
                    } catch (InterruptedException e) {}
                }
                String uuid = UUID.randomUUID().toString();

                Instant now = clock.instant();
                if (now.isAfter(until)) {
                    break;
                }

                try {
                    if (cfg.getTimeout() > 0) {
                        Thread executingThread = Thread.currentThread();
                        long timeoutDelay = computeTimeoutInMillis(cfg, now);
                        tm.register(uuid, timeoutDelay, executingThread);
                    }

                    Object value = context.proceed();
                    if ((cfg.getTimeout() == 0) || !tm.hasReachedTimeout(uuid)) {
                        return value;
                    }
                    latestFailure = new TimeoutException();
                } catch (Throwable t) {
                    latestFailure = t;

                    // AbortOn has priority on RetryOn
                    if (isAssignableToAnyOf(cfg.getAbortOn(), t)) {
                        break;
                    }
                    if (!isAssignableToAnyOf(cfg.getRetryOn(), t)) {
                        break;
                    }
                } finally {
                    tm.cancelTimerByUUID(uuid);
                }

                retry++;
                ended = (retry > cfg.getMaxRetries());
            } while (!ended);

        if (latestFailure instanceof FaultToleranceException) {
            throw (FaultToleranceException)latestFailure;
        }
        throw new FaultToleranceException(latestFailure);
    }

    private boolean isAssignableToAnyOf(Class<? extends Throwable>[] abortOn, Throwable t) {
        return Arrays.asList(abortOn).stream().filter(c -> c.isInstance(t)).findFirst().isPresent();
    }

    private long computeJitterInMillis(InvocationConfiguration cfg, Instant baseInstant) {
        return Duration.between(baseInstant, cfg.getJitterDelayUnit().addTo(baseInstant, rnd.nextInt((int)cfg.getJitter()))).toMillis();
    }

    private long computeTimeoutInMillis(InvocationConfiguration cfg, Instant baseInstant) {
        return Duration.between(baseInstant, cfg.getTimeoutUnit().addTo(baseInstant, cfg.getTimeout())).toMillis();
    }

    private long computeDelayInMillis(InvocationConfiguration cfg, Instant baseInstant) {
        return Duration.between(baseInstant, cfg.getDelayUnit().addTo(baseInstant, cfg.getDelay())).toMillis();
    }
}
