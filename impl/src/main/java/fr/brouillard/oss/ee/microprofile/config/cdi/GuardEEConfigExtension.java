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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.Producer;
import javax.inject.Provider;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.brouillard.oss.ee.microprofile.config.GuardEEConfigurator;

public class GuardEEConfigExtension implements Extension {
    private Set<Beanable> toRegisterBeans = new HashSet<>();
    
    public void processInjectionPoints(@Observes ProcessInjectionPoint<?, ?> pip) {
        InjectionPoint ip = pip.getInjectionPoint();

        if (ip.getAnnotated().isAnnotationPresent(ConfigProperty.class)) {
        	if (!isAProducedType(ip.getType())) {
            	ConfigProperty cp = ip.getAnnotated().getAnnotation(ConfigProperty.class);
            	
            	String keyName = GuardEEConfigurator.keyName(ip, cp.name());
            	String defaultValue = cp.defaultValue();
            	BindingConfigProperty bcp = new BindingConfigPropertyAnnotationLiteral(keyName, defaultValue);
            	
            	InjectionPoint wrappedIpWithBindingConfigProperty = InjectionPointWrapper.create().addQualifiers(bcp).wrap(ip);
            	pip.setInjectionPoint(wrappedIpWithBindingConfigProperty);
            	
            	toRegisterBeans.add(new Beanable(ip.getType(), bcp));
        	}
        }
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void registerConfigPropertyBeans(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        for (Beanable bean: toRegisterBeans) {
            abd.addBean(new BindingConfigPropertyBean<>((Class)bean.type, bean.bcp));
        }
    }

    private boolean isAProducedType(Type ipType) {
    	if (ipType instanceof Class) {
    		return isASimpleProducedType(ipType);
    	} else if (ipType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) ipType;
			String rawTypeName = pType.getRawType().getTypeName();
			
			List<String> handledBuilderClasses = Arrays.asList(
					Optional.class.getName()
					, Provider.class.getName()
					, Supplier.class.getName()
					);
			
			return handledBuilderClasses.contains(rawTypeName);
    	}
    	
    	return false;
    }
    private boolean isASimpleProducedType(Type ipType) {
        return ipType == String.class 
                || ipType == Boolean.class
                || ipType == Boolean.TYPE
                || ipType == Integer.class
                || ipType == Integer.TYPE
                || ipType == Long.class
                || ipType == Long.TYPE
                || ipType == Float.class
                || ipType == Float.TYPE
                || ipType == Double.class
                || ipType == Double.TYPE
                || ipType == Duration.class
                || ipType == LocalTime.class
                || ipType == LocalDate.class
                || ipType == LocalDateTime.class
                || ipType == OffsetDateTime.class
                || ipType == OffsetTime.class
                || ipType == Instant.class
                || ipType == URL.class;
    }

    
    private boolean isNotAProducedType(Type ipType) {
        // Should be different from all types produced by Producers#XXXX
    	if (ipType instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) ipType;
		} else {
			return isNotASimpleProducedType(ipType);
		}
    	
        return isNotASimpleProducedType(ipType)
                && ipType != Optional.class
                && ipType != Producer.class
                ;
    }
    
    private boolean isNotASimpleProducedType(Type ipType) {
        return ipType != String.class 
                && ipType != Boolean.class
                && ipType != Boolean.TYPE
                && ipType != Integer.class
                && ipType != Integer.TYPE
                && ipType != Long.class
                && ipType != Long.TYPE
                && ipType != Float.class
                && ipType != Float.TYPE
                && ipType != Double.class
                && ipType != Double.TYPE
                && ipType != Duration.class
                && ipType != LocalTime.class
                && ipType != LocalDate.class
                && ipType != LocalDateTime.class
                && ipType != OffsetDateTime.class
                && ipType != OffsetTime.class
                && ipType != Instant.class
                && ipType != URL.class;
    }

    /*
     * Simple container class 
     */
	private static class Beanable {
		private Type type;
		private BindingConfigProperty bcp;

		public Beanable(Type t, BindingConfigProperty bcp) {
			this.bcp = bcp;
			this.type = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((type == null) ? 0 : type.getTypeName().hashCode());
			result = prime * result + ((bcp == null) ? 0 : bcp.name().hashCode());
			result = prime * result + ((bcp == null) ? 0 : bcp.defaultValue().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Beanable other = (Beanable) obj;
			if (bcp == null) {
				if (other.bcp != null)
					return false;
			} else {
				if (!bcp.name().equals(other.bcp.name()))
					return false;
				else if (!bcp.defaultValue().equals(other.bcp.defaultValue()))
					return false;
			}
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.getTypeName().equals(other.type.getTypeName()))
				return false;
			return true;
		}
	}
}
