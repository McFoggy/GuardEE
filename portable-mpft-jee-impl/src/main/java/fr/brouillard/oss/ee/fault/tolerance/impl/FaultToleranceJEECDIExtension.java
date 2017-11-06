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

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

public class FaultToleranceJEECDIExtension implements Extension {
    private final static List<Class<? extends Annotation>> FT_CLASSES = Arrays.asList(Retry.class, Timeout.class, Fallback.class, Bulkhead.class, CircuitBreaker.class);

    public <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat, BeanManager beanManager) {
        AnnotatedType<T> annotatedType = pat.getAnnotatedType();

        if ("org.eclipse.microprofile.fault.tolerance.tck.retry.clientserver.RetryClientForMaxRetries".equals(annotatedType.getJavaClass().getName())) {
            System.out.println("here");
        }

        boolean needFTAnnotation = anyFTAnnotation(annotatedType.getJavaClass().getAnnotations());

        Iterator<AnnotatedMethod<? super T>> methodIterator = annotatedType.getMethods().iterator();
        while (!needFTAnnotation && methodIterator.hasNext()) {
            AnnotatedMethod<? super T> am = methodIterator.next();
            needFTAnnotation = anyFTAnnotation(am.getJavaMember().getDeclaredAnnotations());
        }

        if (needFTAnnotation) {
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

    private boolean anyFTAnnotation(Annotation[] annotations) {
        return Arrays.asList(annotations).stream().anyMatch(a -> FT_CLASSES.contains(a.annotationType()));
    }
}
