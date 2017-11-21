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
package fr.brouillard.oss.ee.fault.tolerance.misc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

public abstract class Annotations {
    private Annotations() {
    }

    /**
     * Locate an annotation on a method or indirectly on the class the method is declared
     * on or any class/interface belonging to its parent structure.
     * @param m the method on which the annotation is looked for
     * @param annotationClass the annotation class to look for
     * @return an Optional containing the found annotation value or an empty Optional
     */
    public static <T extends Annotation> Optional<T> find(Method m, Class<T> annotationClass) {
        T t = m.getDeclaredAnnotation(annotationClass);

        if (t == null) {
            t = m.getDeclaringClass().getAnnotation(annotationClass);
        }

        return Optional.ofNullable(t);
    }
}
