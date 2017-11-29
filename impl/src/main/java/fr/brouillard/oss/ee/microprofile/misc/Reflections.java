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
package fr.brouillard.oss.ee.microprofile.misc;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.eclipse.microprofile.config.spi.Converter;

public class Reflections {
    public static Type getConverterType(Class forClass) {
        return getSingleParametrizedType(forClass, Converter.class);
    }
    
    public static Type getSingleParametrizedType(Class forClass, Class forType) {
        if (forClass.equals(Object.class)) {
            return null;
        }

        for (Type type : forClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                if (pt.getRawType().equals(forType)) {
                    Type[] typeArguments = pt.getActualTypeArguments();
                    if (typeArguments.length != 1) {
                        throw new IllegalStateException("Class " + forClass + " must have a single parameterized type");
                    }
                    return typeArguments[0];
                }
            }
        }

        return getSingleParametrizedType(forClass.getSuperclass(), forType);
    }
}
