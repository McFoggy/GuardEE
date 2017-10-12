package fr.brouillard.oss.ee.fault.tolerance.model;

import java.time.temporal.ChronoUnit;

import org.eclipse.microprofile.faulttolerance.Retry;

public class RetryConfiguration {
    private int maxRetries = 3;
    private long delay = 0;
    private ChronoUnit delayUnit = ChronoUnit.MILLIS;
    private long maxDuration = 180000;
    private ChronoUnit durationUnit = ChronoUnit.MILLIS;
    private long jitter = 200;
    private ChronoUnit jitterDelayUnit = ChronoUnit.MILLIS;
    private Class<? extends Throwable>[] retryOn = new Class[]{Exception.class};
    private Class<? extends Throwable>[] abortOn = new Class[0];

    private RetryConfiguration() {
    }

    private RetryConfiguration(RetryConfiguration o) {
        maxRetries = o.maxRetries;
        delay = o.delay;
        delayUnit = o.delayUnit;
        maxDuration = o.maxDuration;
        durationUnit = o.durationUnit;
        jitter = o.jitter;
        jitterDelayUnit = o.jitterDelayUnit;
        retryOn = o.retryOn;
        abortOn = o.abortOn;
    }

    public static RetryConfiguration of(Retry o) {
        RetryConfiguration r = new RetryConfiguration();

        r.maxRetries = o.maxRetries();
        r.delay = o.delay();
        r.delayUnit = o.delayUnit();
        r.maxDuration = o.maxDuration();
        r.durationUnit = o.durationUnit();
        r.jitter = o.jitter();
        r.jitterDelayUnit = o.jitterDelayUnit();
        r.retryOn = o.retryOn();
        r.abortOn = o.abortOn();

        return r;
    };

    public int getMaxRetries() {
        return maxRetries;
    }

    public RetryConfiguration setMaxRetries(int maxRetries) {
        RetryConfiguration cfg = new RetryConfiguration(this);
        cfg.maxRetries = maxRetries;
        return cfg;
    }

    public long getDelay() {
        return delay;
    }

    public RetryConfiguration setDelay(long delay) {
        RetryConfiguration cfg = new RetryConfiguration(this);
        cfg.delay = delay;
        return cfg;
    }

    public ChronoUnit getDelayUnit() {
        return delayUnit;
    }

    public RetryConfiguration setDelayUnit(ChronoUnit delayUnit) {
        RetryConfiguration cfg = new RetryConfiguration(this);
        cfg.delayUnit = delayUnit;
        return cfg;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public RetryConfiguration setMaxDuration(long maxDuration) {
        RetryConfiguration cfg = new RetryConfiguration(this);
        cfg.maxDuration = maxDuration;
        return cfg;
    }

    public ChronoUnit getDurationUnit() {
        return durationUnit;
    }

    public RetryConfiguration setDurationUnit(ChronoUnit durationUnit) {
        RetryConfiguration cfg = new RetryConfiguration(this);
        cfg.durationUnit = durationUnit;
        return cfg;
    }

    public long getJitter() {
        return jitter;
    }

    public RetryConfiguration setJitter(long jitter) {
        RetryConfiguration cfg = new RetryConfiguration(this);
        cfg.jitter = jitter;
        return cfg;
    }

    public ChronoUnit getJitterDelayUnit() {
        return jitterDelayUnit;
    }

    public RetryConfiguration setJitterDelayUnit(ChronoUnit jitterDelayUnit) {
        RetryConfiguration cfg = new RetryConfiguration(this);
        cfg.jitterDelayUnit = jitterDelayUnit;
        return cfg;
    }

    public Class<? extends Throwable>[] getRetryOn() {
        return retryOn;
    }

    public RetryConfiguration setRetryOn(Class<? extends Throwable>[] retryOn) {
        RetryConfiguration cfg = new RetryConfiguration(this);
        cfg.retryOn = new Class[retryOn.length];
        System.arraycopy(retryOn, 0, cfg.retryOn, 0, retryOn.length);
        return cfg;
    }

    public Class<? extends Throwable>[] getAbortOn() {
        return abortOn;
    }

    public RetryConfiguration setAbortOn(Class<? extends Throwable>[] abortOn) {
        RetryConfiguration cfg = new RetryConfiguration(this);
        cfg.abortOn = new Class[abortOn.length];
        System.arraycopy(abortOn, 0, cfg.abortOn, 0, abortOn.length);
        return cfg;
    }
}
