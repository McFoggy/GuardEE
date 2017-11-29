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
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Provider;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

import fr.brouillard.oss.ee.microprofile.config.GuardEEConfig;

@ApplicationScoped
public class Producers {
    @Produces
    public Config configuration() {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return ConfigProviderResolver.instance()
                .getBuilder()
                .forClassLoader(tccl)
                .addDiscoveredSources()
                .addDefaultSources()
                .addDiscoveredConverters()
                .build();
    }

    @ConfigProperty
    @Produces
    public String produceStringConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, String.class);
    }
    
    @ConfigProperty
    @Produces
    public Boolean produceBooleanConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, Boolean.class);
    }

    @ConfigProperty
    @Produces
    public Integer produceIntegerConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, Integer.class);
    }

    @ConfigProperty
    @Produces
    public Long produceLongConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, Long.class);
    }

    @ConfigProperty
    @Produces
    public Float produceFloatConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, Float.class);
    }

    @ConfigProperty
    @Produces
    public Double produceDoubleConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, Double.class);
    }

    @ConfigProperty
    @Produces
    public Duration produceDurationConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, Duration.class);
    }

    @ConfigProperty
    @Produces
    public LocalTime produceLocalTimeConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, LocalTime.class);
    }

    @ConfigProperty
    @Produces
    public LocalDate produceLocalDateConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, LocalDate.class);
    }

    @ConfigProperty
    @Produces
    public LocalDateTime produceLocalDateTimeConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, LocalDateTime.class);
    }

    @ConfigProperty
    @Produces
    public OffsetDateTime produceOffsetDateTimeConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, OffsetDateTime.class);
    }

    @ConfigProperty
    @Produces
    public OffsetTime produceOffsetTimeConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, OffsetTime.class);
    }

    @ConfigProperty
    @Produces
    public Instant produceInstantConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, Instant.class);
    }

    @ConfigProperty
    @Produces
    public URL produceURLConfigProperty(InjectionPoint ip) {
        return getConfiguredValue(ip, URL.class);
    }

    @ConfigProperty
    @Produces
    public <T> Optional<T> produceOptional(InjectionPoint ip) {
        Annotated annotated = ip.getAnnotated();
        Type baseType = annotated.getBaseType();

        if (baseType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) baseType;
            Type expectedType = pt.getActualTypeArguments()[0];
            Class<T> type = (Class<T>) expectedType;
            return Optional.ofNullable(getConfiguredValue(ip, type));
        }

        return Optional.empty();
    }

    @ConfigProperty
    @Produces
    public <T> Provider<T> produceProvider(InjectionPoint ip) {
        Annotated annotated = ip.getAnnotated();
        Type baseType = annotated.getBaseType();

        if (baseType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) baseType;
            Type expectedType = pt.getActualTypeArguments()[0];
            Class<T> type = (Class<T>) expectedType;
            return () -> getConfiguredValue(ip, type);
        }

        return () -> null;
    }
    
    public <T> T getConfiguredValue(InjectionPoint ip, Class<T> type) {
        Config config = configuration();
        ConfigProperty configProperty = ip.getAnnotated().getAnnotation(ConfigProperty.class);

        String name = buildConfigKeyName(ip, configProperty);
        if (name == null) {
            return null;
        }

        Optional<T> value = config.getOptionalValue(name, type);
        if (value.isPresent()) {
            return value.get();
        }
        // try with the default value if one is specified
        if (!ConfigProperty.UNCONFIGURED_VALUE.equals(configProperty.defaultValue())) {
            // there is a default value
            // let's convert it using the registered converters of the configuration
            return ((GuardEEConfig)config).convert(configProperty.defaultValue(), type);
        }

        return null;
    }

    private String buildConfigKeyName(InjectionPoint ip, ConfigProperty configProperty) {
        if (!configProperty.name().isEmpty()) {
            return configProperty.name();
        }

        return null;
    }
}
