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
package fr.brouillard.oss.ee.fault.tolerance.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import javax.interceptor.InvocationContext;

public class AnnotationFinder {
    public static <T extends Annotation> AnnotationFindResult<T> find(InvocationContext ic, Class<T> annotationClass) {
        Method invokedMethod = ic.getMethod();
        Object caller = ic.getTarget();
        Class<?> callerClass = caller.getClass();

        return find(annotationClass, callerClass, invokedMethod);
    }

    public static <T extends Annotation> AnnotationFindResult<T> find(Class<T> annotationClass, Class<?> classOfCall, Method invokedMethod) {
        String searchKey = classOfCall.getName() + "/" + invokedMethod.getName();

        // the method is directly declared on the caller
        if (Arrays.asList(classOfCall.getDeclaredMethods()).contains(invokedMethod)) {
            T annotation = invokedMethod.getAnnotation(annotationClass);
            return new AnnotationFindResult(searchKey, searchKey, annotation);
        }
        ;

        return findOnClass(searchKey, classOfCall, invokedMethod, annotationClass);
    }

    private static <T extends Annotation> AnnotationFindResult<T> findOnClass(String searchKey, Class<?> aClass, Method invokedMethod, Class<T> annotationClass) {
        if (aClass == null) {
            return new AnnotationFindResult<>(searchKey, null, null);
        }
        
        if (aClass.getName().equals(invokedMethod.getDeclaringClass().getName())) {
            // we have reached the class that declares the method
            T annotation = invokedMethod.getAnnotation(annotationClass);
            
            if (annotation != null) {
                return new AnnotationFindResult<>(searchKey, aClass.getName() + "/" + invokedMethod.getName(), annotation);
            } else {
                // last chance we look on the class declaring the method
                return new AnnotationFindResult<>(searchKey, aClass.getName(), invokedMethod.getDeclaringClass().getDeclaredAnnotation(annotationClass));
            }
        }

        return findOnClass(searchKey, aClass.getSuperclass(), invokedMethod, annotationClass);
    }
    
    public static class AnnotationFindResult<T extends Annotation> {
        private final String searchKey;
        private final String foundKey;
        private final Optional<T> annotation;

        AnnotationFindResult(String searchKey, String foundKey, T annotationValue) {
            this.searchKey = searchKey;
            this.foundKey = foundKey;
            this.annotation = Optional.ofNullable(annotationValue);
        }

        public String getSearchKey() {
            return searchKey;
        }

        public String getFoundKey() {
            return foundKey;
        }

        public Optional<T> getAnnotation() {
            return annotation;
        }
    }
}
