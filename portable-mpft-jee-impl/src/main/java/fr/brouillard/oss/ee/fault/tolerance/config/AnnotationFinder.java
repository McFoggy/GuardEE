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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import javax.interceptor.InvocationContext;

public class AnnotationFinder {
    public static <T extends Annotation> Optional<T> find(InvocationContext ic, Class<T> annotationClass) {
        Method invokedMethod = ic.getMethod();
        Object caller = ic.getTarget();

        // the method is directly declared on the caller
        if (Arrays.asList(caller.getClass().getDeclaredMethods()).contains(invokedMethod)) {
            return Optional.ofNullable(invokedMethod.getAnnotation(annotationClass));
        };
        
        return findOn(caller.getClass(), invokedMethod, annotationClass);
    }

    private static <T extends Annotation> Optional<T> findOn(Class<?> aClass, Method invokedMethod, Class<T> annotationClass) {
        if (aClass == null) {
            return Optional.empty();
        }
        
        if (aClass.getName().equals(invokedMethod.getDeclaringClass().getName())) {
            // we have reached the class that declares the method
            T annotation = invokedMethod.getAnnotation(annotationClass);
            
            if (annotation != null) {
                return Optional.of(annotation);
            } else {
                // last chance we look on the class declaring the method
                return Optional.ofNullable(invokedMethod.getDeclaringClass().getDeclaredAnnotation(annotationClass));
            }
        }

        return findOn(aClass.getSuperclass(), invokedMethod, annotationClass);
    }
}
