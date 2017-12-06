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
package fr.brouillard.oss.ee.microprofile.config;

import java.util.Optional;

import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.InjectionPoint;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

public class GuardEEConfigurator {
    public static String keyName(InjectionPoint ip, String configPropertyName) {
        if (!configPropertyName.isEmpty()) {
            return configPropertyName;
        }
        
        if (ip.getAnnotated() instanceof AnnotatedField) {
        	AnnotatedField af = (AnnotatedField)ip.getAnnotated();
        	String className = af.getDeclaringType().getJavaClass().getName().replace('$', '.');
        	String fieldName = af.getJavaMember().getName();
			return className + "." + fieldName;
		}
        
        // TODO build default key from injection point
        return "";
    }

	public static Config buildConfiguration() {
	    ClassLoader tccl = Thread.currentThread().getContextClassLoader();
	    return ConfigProviderResolver.instance()
	            .getBuilder()
	            .forClassLoader(tccl)
	            .addDiscoveredSources()
	            .addDefaultSources()
	            .addDiscoveredConverters()
	            .build();
	}

	public static <T> T getConfiguredValue(InjectionPoint ip, Class<T> type) {
	    ConfigProperty cp = ip.getAnnotated().getAnnotation(ConfigProperty.class);
	    String key = keyName(ip, cp.name());
	    
	    return getConfiguredValue(key, cp.defaultValue(), type);
	}

	public static <T> T getConfiguredValue(String key, String defaultValue, Class<T> of) {
		Config config = buildConfiguration();
		
	    Optional<T> value = config.getOptionalValue(key, of);
	    if (value.isPresent()) {
	        return value.get();
	    }
	    
	    // try with the default value if one is specified
	    if (!ConfigProperty.UNCONFIGURED_VALUE.equals(defaultValue)) {
	        // there is a default value
	        // let's convert it using the registered converters of the configuration
	        return ((GuardEEConfig)config).convert(defaultValue, of);
	    }
	
	    return null;
	}
}
