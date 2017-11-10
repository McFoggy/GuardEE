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
package fr.brouillard.oss.ee.fault.tolerance.config;

import java.time.temporal.ChronoUnit;

import org.eclipse.microprofile.faulttolerance.Retry;

public class RetryContext {
    private int maxRetries = 0;
    private long delay = 0;
    private ChronoUnit delayUnit = ChronoUnit.MILLIS;
    private long maxDuration = 0;
    private ChronoUnit durationUnit = ChronoUnit.MILLIS;
    private long jitter = 0;
    private ChronoUnit jitterDelayUnit = ChronoUnit.MILLIS;
    private Class<? extends Throwable>[] retryOn = new Class[]{Exception.class};
    private Class<? extends Throwable>[] abortOn = new Class[0];
    
    public RetryContext(Retry r) {
        this.maxRetries = r.maxRetries();
        this.delay = r.delay();
        this.delayUnit = r.delayUnit();
        this.maxDuration = r.maxDuration();
        this.durationUnit = r.durationUnit();
        this.jitter = r.jitter();
        this.jitterDelayUnit = r.jitterDelayUnit();
        this.retryOn = r.retryOn();
        this.abortOn = r.abortOn();
    }

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

    public Class<? extends Throwable>[] getRetryOn() {
        return retryOn;
    }

    public Class<? extends Throwable>[] getAbortOn() {
        return abortOn;
    }
}
