/**
 * Copyright © 2017 Matthieu Brouillard [http://oss.brouillard.fr/GuardEE] (matthieu@brouillard.fr)
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
package fr.brouillard.oss.ee.fault.tolerance.model;

import java.time.temporal.ChronoUnit;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

public class InvocationConfiguration {
    private int maxRetries = 0;
    private long delay = 0;
    private ChronoUnit delayUnit = ChronoUnit.MILLIS;
    private long maxDuration = 0;
    private ChronoUnit durationUnit = ChronoUnit.MILLIS;
    private long jitter = 0;
    private ChronoUnit jitterDelayUnit = ChronoUnit.MILLIS;
    private long timeout = 0;
    private ChronoUnit timeoutUnit = ChronoUnit.MILLIS;
    private Class<? extends Throwable>[] retryOn = new Class[]{Exception.class};
    private Class<? extends Throwable>[] abortOn = new Class[0];

    private InvocationConfiguration() {
    }

    private InvocationConfiguration(InvocationConfiguration o) {
        maxRetries = o.maxRetries;
        delay = o.delay;
        delayUnit = o.delayUnit;
        maxDuration = o.maxDuration;
        durationUnit = o.durationUnit;
        jitter = o.jitter;
        jitterDelayUnit = o.jitterDelayUnit;
        timeout = o.timeout;
        timeoutUnit = o.timeoutUnit;
        retryOn = o.retryOn;
        abortOn = o.abortOn;
    }

    public InvocationConfiguration with(Retry o) {
        InvocationConfiguration ic = new InvocationConfiguration(this);

        // let's override the Retry properties
        ic.maxRetries = o.maxRetries();
        ic.delay = o.delay();
        ic.delayUnit = o.delayUnit();
        ic.maxDuration = o.maxDuration();
        ic.durationUnit = o.durationUnit();
        ic.jitter = o.jitter();
        ic.jitterDelayUnit = o.jitterDelayUnit();
        ic.retryOn = o.retryOn();
        ic.abortOn = o.abortOn();

        return ic;
    };

    public InvocationConfiguration with(Timeout o) {
        InvocationConfiguration ic = new InvocationConfiguration(this);

        // let's override Timeout properties
        ic.timeout = o.value();
        ic.timeoutUnit = o.unit();

        return ic;
    };

    public static InvocationConfiguration any() {
        return new InvocationConfiguration();
    };

    public static InvocationConfiguration of(Retry o) {
        InvocationConfiguration r = new InvocationConfiguration();

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

    public long getDelay() {
        return delay;
    }

    public ChronoUnit getDelayUnit() {
        return delayUnit;
    }

    public long getMaxDuration() {
        return maxDuration;
    }

    public ChronoUnit getDurationUnit() {
        return durationUnit;
    }

    public long getJitter() {
        return jitter;
    }

    public ChronoUnit getJitterDelayUnit() {
        return jitterDelayUnit;
    }

    public long getTimeout() {
        return timeout;
    }

    public ChronoUnit getTimeoutUnit() {
        return timeoutUnit;
    }

    public Class<? extends Throwable>[] getRetryOn() {
        return retryOn;
    }

    public Class<? extends Throwable>[] getAbortOn() {
        return abortOn;
    }
}
