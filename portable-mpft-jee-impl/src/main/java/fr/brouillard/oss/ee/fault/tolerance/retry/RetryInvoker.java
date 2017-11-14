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
package fr.brouillard.oss.ee.fault.tolerance.retry;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.faulttolerance.exceptions.CircuitBreakerOpenException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.brouillard.oss.ee.fault.tolerance.EEGuardException;
import fr.brouillard.oss.ee.fault.tolerance.config.Configurator;
import fr.brouillard.oss.ee.fault.tolerance.config.RetryContext;
import fr.brouillard.oss.ee.fault.tolerance.impl.Invoker;
import fr.brouillard.oss.ee.fault.tolerance.impl.InvokerChain;
import fr.brouillard.oss.ee.fault.tolerance.misc.Exceptions;

public class RetryInvoker implements Invoker {
    private final static Logger LOGGER  = LoggerFactory.getLogger(RetryInvoker.class); 
    private final Configurator conf;
    private final Random rnd;

    @Inject
    public RetryInvoker(Configurator cfg) {
        this.conf = cfg;
        this.rnd = new Random(System.nanoTime());
    }

    @Override
    public Object invoke(InvocationContext context, InvokerChain chain) throws Exception {
        RetryContext cfg = conf.retry(context).orElseThrow(() -> new EEGuardException());

        boolean ended = true;
        int retry = 0;
        Exception latestFailure = null;

        long jitterInMillis = 0;
        long durationInMillis = Duration.of(cfg.getMaxDuration(), cfg.getDurationUnit()).toMillis();
        long delayInMillis = Duration.of(cfg.getDelay(), cfg.getDelayUnit()).toMillis();
        long durationExpirationTime = System.currentTimeMillis() + durationInMillis;

        do {
            LOGGER.trace("{}#{} start retry[{}]"
                    , context.getTarget().getClass().getSimpleName()
                    , context.getMethod().getName(), retry);
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

            try {
                Object result = chain.invoke(context);
                LOGGER.debug("{}#{} succeed after {} retries"
                        , context.getTarget().getClass().getSimpleName()
                        , context.getMethod().getName()
                        , retry);
                return result;
            } catch (Exception t) {
                latestFailure = t;
                LOGGER.trace("{}#{} failed retry[{}], exception: {}"
                        , context.getTarget().getClass().getSimpleName()
                        , context.getMethod().getName()
                        , retry
                        , latestFailure.getMessage());

                // AbortOn has priority on RetryOn
                
                // Lets stop execution if:
                // - CircuitBreaker was opened 
                // - or if received throwable was configured to stop retry executions 
                boolean shouldStopExecution = CircuitBreakerOpenException.class.isInstance(latestFailure) || Exceptions.isAssignableToAnyOf(cfg.getAbortOn(), latestFailure);
                if (shouldStopExecution) {
                    LOGGER.trace("exception[{}] makes the retry process to stop at retry[{}]"
                            , latestFailure.getClass().getSimpleName()
                            , retry);
                    break;
                }

                // Lets continue
                // - We received a TimeoutException from TimeoutInvoker 
                // - or if received throwable was configured to retry executions 
                boolean continueExecution = TimeoutException.class.isInstance(latestFailure) || Exceptions.isAssignableToAnyOf(cfg.getRetryOn(), latestFailure);
                if (!continueExecution) {
                    LOGGER.trace("unexpected exception[{}] makes the retry process to stop at retry[{}]"
                            , latestFailure.getClass().getSimpleName()
                            , retry);
                    break;
                }
            }

            retry++;
            ended = (retry > cfg.getMaxRetries());
        } while (!ended);

        LOGGER.debug("{}#{} failed after {} retries, exception: {}"
                , context.getTarget().getClass().getSimpleName()
                , context.getMethod().getName()
                , retry
                , latestFailure.getMessage());
        LOGGER.trace("detailled failure exception", latestFailure);
        throw latestFailure;
    }

    private long computeJitterInMillis(RetryContext cfg) {
        long jitter = Duration.of(cfg.getJitter(), cfg.getJitterDelayUnit()).toMillis();
        return rnd.nextInt((int) jitter);
    }
}
