package fr.brouillard.oss.ee.fault.tolerance.impl;

import org.eclipse.microprofile.faulttolerance.*;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

            AnnotatedTypeWrapper<T> wrapper = new AnnotatedTypeWrapper<T>(
                    annotatedType, annotatedType.getAnnotations());
            wrapper.addAnnotation(ftJEEAnnotation);
            pat.setAnnotatedType(wrapper);
        }
    }

    private boolean anyFTAnnotation(Annotation[] annotations) {
        return Arrays.asList(annotations).stream().anyMatch(a -> FT_CLASSES.contains(a.annotationType()));
    }
}
