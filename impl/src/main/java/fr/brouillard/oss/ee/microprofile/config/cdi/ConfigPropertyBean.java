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
package fr.brouillard.oss.ee.microprofile.config.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public class ConfigPropertyBean<T> implements Bean<T>, PassivationCapable {
    private final BeanManager beanManager;
    private final Class<T> objectClass;

    public ConfigPropertyBean(BeanManager bm, Class<T> objectClass) {
        this.beanManager = bm;
        this.objectClass = objectClass;
    }
    
    @Override
    public Class<?> getBeanClass() {
        return ConfigPropertyBean.class;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.EMPTY_SET;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
        Set<Bean<?>> beans = beanManager.getBeans(InjectionPoint.class);
        Bean<?> bean = beanManager.resolve(beans);
        InjectionPoint ip = (InjectionPoint) beanManager.getReference(bean, InjectionPoint.class,  creationalContext);
        Producers producers = (Producers) beanManager.getReference(bean, Producers.class,  creationalContext);
        
        if (ip == null) {
            throw new IllegalStateException("cannot retrieve InjectionPoint from current CDI context");
        }
        if (producers == null) {
            throw new IllegalStateException("cannot retrieve instance of Producers from current CDI context");
        }

        Annotated annotated = ip.getAnnotated();
        Class<T> baseType = (Class<T>) annotated.getBaseType();
        
        return producers.getConfiguredValue(ip, baseType);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {

    }

    @Override
    public Set<Type> getTypes() {
        return Collections.singleton(objectClass);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return BEAN_QUALIFIERS;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public String getId() {
        return ConfigPropertyBean.class.getName() + "_" + objectClass;
    }
    
    private final static Set<Annotation> BEAN_QUALIFIERS = new HashSet<>();
    static {
        BEAN_QUALIFIERS.add(new ConfigPropertyAnnotationLiteral());
    }
}
