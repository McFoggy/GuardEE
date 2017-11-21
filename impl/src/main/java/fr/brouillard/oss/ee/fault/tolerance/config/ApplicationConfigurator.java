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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class ApplicationConfigurator implements Configurator {
    private final static Logger LOGGER = LoggerFactory.getLogger(ApplicationConfigurator.class);

    private ReentrantReadWriteLock rw;
    private Map<String, BulkheadContext> bulkheadContexts;

    @PostConstruct
    public void initialize() {
        rw = new ReentrantReadWriteLock(true);
        bulkheadContexts = new LinkedHashMap<>();
    }

    @Override
    public Optional<TimeoutContext> timeout(InvocationContext ic) {
        Optional<Timeout> timeoutAnnotation = AnnotationFinder.find(ic, Timeout.class).getAnnotation();
        return timeoutAnnotation.map(t -> new TimeoutContext(t.value(), t.unit()));
    }

    @Override
    public Optional<RetryContext> retry(InvocationContext ic) {
        Optional<Retry> retryAnnotation = AnnotationFinder.find(ic, Retry.class).getAnnotation();
        return retryAnnotation.map(RetryContext::new);
    }

    @Override
    public Optional<BulkheadContext> bulkhead(InvocationContext ic) {
        AnnotationFinder.AnnotationFindResult<Bulkhead> bulkheadAnnotationFindResult = AnnotationFinder.find(ic, Bulkhead.class);
        Optional<Asynchronous> optAsynchronuous = AnnotationFinder.find(ic, Asynchronous.class).getAnnotation();

        ReentrantReadWriteLock.ReadLock readLock = rw.readLock();

        if (bulkheadAnnotationFindResult.getAnnotation().isPresent()) {
            BulkheadContext context = bulkheadContexts.get(bulkheadAnnotationFindResult.getSearchKey());
            
            try {
                if (readLock.tryLock() || readLock.tryLock(5, TimeUnit.SECONDS)) {
                    try {
                        if (context == null) {
                            context = bulkheadContexts.get(bulkheadAnnotationFindResult.getFoundKey());
                        }

                        if (context != null) {
                            LOGGER.debug("for [{}], found bulkhead context: {}", bulkheadAnnotationFindResult.getSearchKey(), context);
                            return Optional.of(context);
                        }
                    } finally {
                        readLock.unlock();
                    }

                    // no context found on the key, we need to create & register one
                    ReentrantReadWriteLock.WriteLock writeLock = rw.writeLock();
                    if (writeLock.tryLock() || writeLock.tryLock(5, TimeUnit.SECONDS)) {
                        try {
                            // check we are not unblocked after another thread was creating the context
                            context = bulkheadContexts.get(bulkheadAnnotationFindResult.getSearchKey());
                            if (context == null) {
                                context = bulkheadContexts.get(bulkheadAnnotationFindResult.getFoundKey());
                            }
                            if (context == null) {
                                context = new BulkheadContext(bulkheadAnnotationFindResult.getAnnotation().get(), optAsynchronuous.isPresent());
                                LOGGER.debug("for [{}], registering under [{}] bulkhead context: {}", bulkheadAnnotationFindResult.getSearchKey(), bulkheadAnnotationFindResult.getFoundKey(), context);

                                bulkheadContexts.put(bulkheadAnnotationFindResult.getFoundKey(), context);
                            }
                            return Optional.of(context);
                        } finally {
                            writeLock.unlock();
                        }
                    } else {
                        return Optional.empty();
                    }
                } else {
                    return Optional.empty();
                }
            } catch (InterruptedException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
