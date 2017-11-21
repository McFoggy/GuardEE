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
package fr.brouillard.oss.ee.fault.tolerance.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Duration;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.DeploymentException;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import fr.brouillard.oss.ee.fault.tolerance.config.AnnotationFinder;

public class FaultToleranceJEECDIExtension implements Extension {
    public <T> void processAnnotatedType(@Observes @WithAnnotations({Retry.class, Timeout.class, Fallback.class, Bulkhead.class, CircuitBreaker.class}) ProcessAnnotatedType<T> pat, BeanManager beanManager) {
        AnnotatedType<T> annotatedType = pat.getAnnotatedType();
        
        checkAnnotatedTypeAnnotations(annotatedType);
        
        if (!annotatedType.isAnnotationPresent(FaultToleranceJEE.class)) {
            Annotation ftJEEAnnotation = new Annotation() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return FaultToleranceJEE.class;
                }
            };

            AnnotatedTypeWrapper<T> wrapper = new AnnotatedTypeWrapper<T>(annotatedType, annotatedType.getAnnotations());
            wrapper.addAnnotation(ftJEEAnnotation);
            pat.setAnnotatedType(wrapper);
        }
    }

    private <T> void checkAnnotatedTypeAnnotations(AnnotatedType<T> annotatedType) {
        Method[] methods = annotatedType.getJavaClass().getMethods();
        Class<T> studyClass = annotatedType.getJavaClass();

        for (Method m: methods) {
            checkAnnotations(studyClass, m);
        }
    }

    private <T> void checkAnnotations(Class<T> studyClass, Method m) {
        checkRetry(studyClass, m);
        checkCircuitBreaker(studyClass, m);
        checkAsynchronous(studyClass, m);
        checkFallback(studyClass, m);
        checkBulkhead(studyClass, m);
    }

    private <T> void checkBulkhead(Class<T> studyClass, Method m) {
        AnnotationFinder.AnnotationFindResult<Bulkhead> result = AnnotationFinder.find(Bulkhead.class, studyClass, m);
        
        if (result.getAnnotation().isPresent()) {
            boolean isAsynchronous = AnnotationFinder.find(Asynchronous.class, studyClass, m).getAnnotation().isPresent();
            Bulkhead bulkhead = result.getAnnotation().get();

            if (bulkhead.value() < 1) {
                throw new DeploymentException(String.format("%s#%s uses bad value for Bulkhead#value: %d"
                        , studyClass.getName()
                        , m.getName()
                        , bulkhead.value()
                ));
            }
            if (isAsynchronous && bulkhead.waitingTaskQueue() < 1) {
                throw new DeploymentException(String.format("%s#%s uses bad value for Bulkhead#waitingTaskQueue: %d"
                        , studyClass.getName()
                        , m.getName()
                        , bulkhead.waitingTaskQueue()
                ));
            }
        }
    }

    private <T> void checkFallback(Class<T> studyClass, Method m) {
        // nothing deploy time for the moment
    }

    private <T> void checkAsynchronous(Class<T> studyClass, Method m) {
        // nothing deploy time for the moment
    }

    private <T> void checkCircuitBreaker(Class<T> studyClass, Method m) {
        AnnotationFinder.AnnotationFindResult<CircuitBreaker> result = AnnotationFinder.find(CircuitBreaker.class, studyClass, m);

        if (result.getAnnotation().isPresent()) {
            CircuitBreaker circuitBreaker = result.getAnnotation().get();
            
            if (circuitBreaker.delay() < 0) {
                throw new DeploymentException(String.format("%s#%s uses bad value for CircuitBreaker#delay: %d"
                        , studyClass.getName()
                        , m.getName()
                        , circuitBreaker.delay()
                ));
            }
            
            if (circuitBreaker.requestVolumeThreshold() < 1) {
                throw new DeploymentException(String.format("%s#%s uses bad value for CircuitBreaker#requestVolumeThreshold: %d"
                        , studyClass.getName()
                        , m.getName()
                        , circuitBreaker.requestVolumeThreshold()
                ));
            }
            
            if (! (circuitBreaker.failureRatio() > 0.0d && circuitBreaker.failureRatio() <= 1.0d)) {
                throw new DeploymentException(String.format("%s#%s uses bad value for CircuitBreaker#failureRatio: %f must be in ]0,1]"
                        , studyClass.getName()
                        , m.getName()
                        , circuitBreaker.failureRatio()
                ));
            }

            if (circuitBreaker.successThreshold() < 1) {
                throw new DeploymentException(String.format("%s#%s uses bad value for CircuitBreaker#successThreshold: %d"
                        , studyClass.getName()
                        , m.getName()
                        , circuitBreaker.successThreshold()
                ));
            }
        }
    }

    private <T> void checkRetry(Class<T> studyClass, Method m) {
        AnnotationFinder.AnnotationFindResult<Retry> result = AnnotationFinder.find(Retry.class, studyClass, m);
        
        if (result.getAnnotation().isPresent()) {
            Retry retry = result.getAnnotation().get();
            
            if (retry.delay() < 0) {
                throw new DeploymentException(String.format("%s#%s uses bad value for Retry#delay: %d"
                        , studyClass.getName()
                        , m.getName()
                        , retry.delay()
                ));
            }
            if (retry.maxRetries() < 0) {
                throw new DeploymentException(String.format("%s#%s uses bad value for Retry#maxRetries: %d"
                        , studyClass.getName()
                        , m.getName()
                        , retry.maxRetries()
                ));
            }
            
            if (retry.maxDuration() != 0) {
                long maxDurationMillis = Duration.of(retry.maxDuration(), retry.durationUnit()).toMillis();
                long delayMillis = Duration.of(retry.delay(), retry.delayUnit()).toMillis();
                if (maxDurationMillis <= delayMillis) {
                    throw new DeploymentException(String.format("%s#%s Retry#maxDuration is lower than Retry#delay: %d(%s) <= %d(%s)"
                            , studyClass.getName(), m.getName()
                            , retry.maxDuration()
                            , retry.durationUnit().name()
                            , retry.delay()
                            , retry.delayUnit()
                    ));
                }
            }
            
            if (retry.jitter() < 0) {
                throw new DeploymentException(String.format("%s#%s uses bad value for Retry#jitter: %d"
                        , studyClass.getName()
                        , m.getName()
                        , retry.jitter()
                ));
            }
            
            if (retry.retryOn().length == 0) {
                throw new DeploymentException(String.format("%s#%s does not define any Throwable in Retry#retryOn"
                        , studyClass.getName()
                        , m.getName()
                ));
            }
        }
    }
}
