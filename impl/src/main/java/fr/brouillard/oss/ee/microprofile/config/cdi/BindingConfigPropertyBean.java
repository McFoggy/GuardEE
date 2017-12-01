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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;

import fr.brouillard.oss.ee.microprofile.config.GuardEEConfigurator;

public class BindingConfigPropertyBean<T> implements Bean<T>, PassivationCapable {
    private final Class<T> of;
    private final Set<Annotation> qualifiers;
	private final Set<Type> types;
	private final BindingConfigProperty bindingConfigProperty;
	private final String name;

    public BindingConfigPropertyBean(Class<T> of, BindingConfigProperty bcp) {
        this.of = of;
        this.bindingConfigProperty = bcp;
        this.qualifiers = new HashSet<>(Arrays.asList(bcp, new ConfigPropertyAnnotationLiteral()));
        this.types = new HashSet<>(Arrays.asList(of, Object.class));
        
        this.name = BindingConfigProperty.class.getName() + "_" + of.getClass() + "#" + bcp.hashCode();
    }
    
    @Override
    public Class<?> getBeanClass() {
        return of;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public T create(CreationalContext<T> creationalContext) {
    	return GuardEEConfigurator.getConfiguredValue(
    			bindingConfigProperty.name()
    			, bindingConfigProperty.defaultValue()
    			, of
    	);
    	
//        Set<Bean<?>> beans = beanManager.getBeans(InjectionPoint.class);
//        Bean<?> bean = beanManager.resolve(beans);
//
//        InjectionPoint ip = (InjectionPoint) beanManager.getReference(bean, InjectionPoint.class,  creationalContext);
//        
//        if (ip == null) {
//            throw new IllegalStateException("cannot retrieve InjectionPoint from current CDI context");
//        }
//        
//        Annotated annotated = ip.getAnnotated();
//        Class<T> baseType = (Class<T>) annotated.getBaseType();
//        
//        return Producers.getConfiguredValue(ip, baseType);
    }

    @Override
    public void destroy(T instance, CreationalContext<T> creationalContext) {
    	creationalContext.release();
    }

    @Override
    public Set<Type> getTypes() {
        return types;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return Dependent.class;
    }

    @Override
    public String getName() {
        return name;
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
        return name;
    }
}
