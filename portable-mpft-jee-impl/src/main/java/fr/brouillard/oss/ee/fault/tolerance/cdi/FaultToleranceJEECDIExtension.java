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
package fr.brouillard.oss.ee.fault.tolerance.cdi;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

public class FaultToleranceJEECDIExtension implements Extension {
    public <T> void processAnnotatedType(@Observes @WithAnnotations({Retry.class, Timeout.class, Fallback.class, Bulkhead.class, CircuitBreaker.class}) ProcessAnnotatedType<T> pat, BeanManager beanManager) {
        AnnotatedType<T> annotatedType = pat.getAnnotatedType();
        
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
}
