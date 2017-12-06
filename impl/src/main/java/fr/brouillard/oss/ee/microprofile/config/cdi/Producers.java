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
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Provider;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import fr.brouillard.oss.ee.microprofile.config.GuardEEConfigurator;

@ApplicationScoped
public class Producers {
    @Produces
    public Config configuration() {
        return GuardEEConfigurator.buildConfiguration();
    }
    
    @ConfigProperty
    @Produces
    public String produceStringConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, String.class);
    }
    
    @ConfigProperty
    @Produces
    public Boolean produceBooleanConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, Boolean.class);
    }

    @ConfigProperty
    @Produces
    public Integer produceIntegerConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, Integer.class);
    }

    @ConfigProperty
    @Produces
    public Long produceLongConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, Long.class);
    }

    @ConfigProperty
    @Produces
    public Float produceFloatConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, Float.class);
    }

    @ConfigProperty
    @Produces
    public Double produceDoubleConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, Double.class);
    }

    @ConfigProperty
    @Produces
    public Duration produceDurationConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, Duration.class);
    }

    @ConfigProperty
    @Produces
    public LocalTime produceLocalTimeConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, LocalTime.class);
    }

    @ConfigProperty
    @Produces
    public LocalDate produceLocalDateConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, LocalDate.class);
    }

    @ConfigProperty
    @Produces
    public LocalDateTime produceLocalDateTimeConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, LocalDateTime.class);
    }

    @ConfigProperty
    @Produces
    public OffsetDateTime produceOffsetDateTimeConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, OffsetDateTime.class);
    }

    @ConfigProperty
    @Produces
    public OffsetTime produceOffsetTimeConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, OffsetTime.class);
    }

    @ConfigProperty
    @Produces
    public Instant produceInstantConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, Instant.class);
    }

    @ConfigProperty
    @Produces
    public URL produceURLConfigProperty(InjectionPoint ip) {
        return GuardEEConfigurator.getConfiguredValue(ip, URL.class);
    }

    @SuppressWarnings("unchecked")
	@ConfigProperty
    @Produces
    public <T> Optional<T> produceOptional(InjectionPoint ip) {
        Annotated annotated = ip.getAnnotated();
        Type baseType = annotated.getBaseType();

        if (baseType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) baseType;
            Type expectedType = pt.getActualTypeArguments()[0];
            Class<T> type = (Class<T>) expectedType;
            return Optional.ofNullable(GuardEEConfigurator.getConfiguredValue(ip, type));
        }

        return Optional.empty();
    }
//
//    @SuppressWarnings("unchecked")
//	@ConfigProperty
//    @Produces
//    public <T> Provider<T> produceProvider(InjectionPoint ip) {
//        Annotated annotated = ip.getAnnotated();
//        Type baseType = annotated.getBaseType();
//
//        if (baseType instanceof ParameterizedType) {
//            ParameterizedType pt = (ParameterizedType) baseType;
//            Type expectedType = pt.getActualTypeArguments()[0];
//            Class<T> type = (Class<T>) expectedType;
//            return () -> GuardEEConfigurator.getConfiguredValue(ip, type);
//        }
//
//        return () -> null;
//    }

    @SuppressWarnings("unchecked")
	@ConfigProperty
    @Produces
    public <T> Supplier<T> produceSupplier(InjectionPoint ip) {
        Annotated annotated = ip.getAnnotated();
        Type baseType = annotated.getBaseType();

        if (baseType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) baseType;
            Type expectedType = pt.getActualTypeArguments()[0];
            Class<T> type = (Class<T>) expectedType;
            return () -> GuardEEConfigurator.getConfiguredValue(ip, type);
        }

        return () -> null;
    }
}
