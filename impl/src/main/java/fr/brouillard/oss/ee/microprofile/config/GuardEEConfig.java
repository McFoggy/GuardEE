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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

import fr.brouillard.oss.ee.microprofile.misc.Reflections;

public class GuardEEConfig implements Config {
    private final List<ConfigSource> sources;
    private final Map<Type, Converter<?>> convertersByType;

    public GuardEEConfig(List<ConfigSource> sources, List<Converter<?>> converters) {
        this.sources = new ArrayList<>(sources);
        this.convertersByType = new HashMap<>();
        
        registerConverters(converters);
        Collections.sort(this.sources, Comparator.comparing(ConfigSource::getOrdinal).reversed());
    }

    private void registerConverters(List<Converter<?>> converters) {
        for (Converter<?> converter : converters) {
            Type type = Reflections.getConverterType(converter.getClass());
            convertersByType.put(type, converter);
        }
    }

    @Override
    public <T> T getValue(String propertyName, Class<T> propertyType) {
        return getOptionalValue(propertyName, propertyType)
                .orElseThrow(() -> new NoSuchElementException("no property " + propertyName + " found"));
    }

    @SuppressWarnings("unchecked")
	public <T> T convert(String value, Class<T> propertyType) {
        if (String.class == propertyType) {
            return (T)value;
        }
        
        Converter<?> converter = convertersByType.computeIfAbsent(propertyType, (t) -> GuardEEConfigConverters.forType(propertyType));
        return (T)converter.convert(value);
    }

    @Override
    public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
        for (ConfigSource source : sources) {
            String value = source.getValue(propertyName);
            if (value != null) {
                return Optional.of(convert(value, propertyType));
            }
        }

        return Optional.empty();
    }

    @Override
    public Iterable<String> getPropertyNames() {
        Set<String> propertyNames = new LinkedHashSet<>();
        sources.stream().map(ConfigSource::getPropertyNames).forEach(propertyNames::addAll);
        return propertyNames;
    }

    @Override
    public Iterable<ConfigSource> getConfigSources() {
        return new ArrayList<>(sources);
    }
}
