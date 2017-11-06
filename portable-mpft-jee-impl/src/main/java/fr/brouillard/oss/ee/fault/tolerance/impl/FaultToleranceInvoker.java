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
package fr.brouillard.oss.ee.fault.tolerance.impl;

import fr.brouillard.oss.ee.fault.tolerance.circuit_breaker.CircuitBreakerHandler;
import fr.brouillard.oss.ee.fault.tolerance.circuit_breaker.CircuitBreakerManager;
import fr.brouillard.oss.ee.fault.tolerance.misc.Exceptions;
import fr.brouillard.oss.ee.fault.tolerance.model.InvocationConfiguration;

import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@ApplicationScoped
public class FaultToleranceInvoker {
    private Random rnd;

    @Inject
    TimeoutManager tm;

    @Inject
    CircuitBreakerManager circuitBreaker;

    public FaultToleranceInvoker() {
    }

    @PostConstruct
    public void initialize() {
        rnd = new Random(System.nanoTime());
    }

    public Object invoke(InvocationConfiguration cfg, InvocationContext context) throws Exception {
        boolean ended = true;
        int retry = 0;
        Throwable latestFailure = null;
        
        long jitterInMillis = 0;
        long durationInMillis = Duration.of(cfg.getMaxDuration(), cfg.getDurationUnit()).toMillis();
        long delayInMillis = Duration.of(cfg.getDelay(), cfg.getDelayUnit()).toMillis();
        long timeoutInMillis = Duration.of(cfg.getTimeout(), cfg.getTimeoutUnit()).toMillis();
        long durationExpirationTime = System.currentTimeMillis() + durationInMillis;

        ExecutionContextImpl executionContext = new ExecutionContextImpl(context.getMethod(), context.getParameters());

        do {
            if (retry > 0 && cfg.getMaxRetries() > 0) {
                jitterInMillis = computeJitterInMillis(cfg);
                try {
                    Thread.sleep(delayInMillis + jitterInMillis);
                } catch (InterruptedException e) {
                }
            }
            String uuid = UUID.randomUUID().toString();

            long now = System.currentTimeMillis();
            if (now > durationExpirationTime) {
                break;
            }

            CircuitBreakerHandler circuitBreakerHandler = circuitBreaker.forContext(executionContext);
            try {
                circuitBreakerHandler.enter();

                if (cfg.getTimeout() > 0) {
                    Thread executingThread = Thread.currentThread();
                    tm.register(uuid, timeoutInMillis, executingThread);
                }

                Object value = context.proceed();
                circuitBreakerHandler.success();
                if ((cfg.getTimeout() == 0) || !tm.hasReachedTimeout(uuid)) {
                    return value;
                }
                latestFailure = new TimeoutException();
            } catch (Throwable t) {
                latestFailure = circuitBreakerHandler.onFailure(t);

                // AbortOn has priority on RetryOn
                if (Exceptions.isAssignableToAnyOf(cfg.getAbortOn(), t)) {
                    break;
                }
                if (!Exceptions.isAssignableToAnyOf(cfg.getRetryOn(), t)) {
                    break;
                }
            } finally {
                tm.cancelTimerByUUID(uuid);
            }

            retry++;
            ended = (retry > cfg.getMaxRetries());
        } while (!ended);

        if (latestFailure instanceof FaultToleranceException) {
            throw (FaultToleranceException) latestFailure;
        } else if (latestFailure instanceof Exception) {
            throw (Exception) latestFailure;
        }
        throw (Error) latestFailure;
    }

    private long computeJitterInMillis(InvocationConfiguration cfg) {
        long jitter = Duration.of(cfg.getJitter(), cfg.getJitterDelayUnit()).toMillis();
        return rnd.nextInt((int) jitter);
    }

}
