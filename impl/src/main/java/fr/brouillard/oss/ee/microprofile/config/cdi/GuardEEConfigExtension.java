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

import java.lang.reflect.Type;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.Producer;

import org.eclipse.microprofile.config.inject.ConfigProperty;

public class GuardEEConfigExtension implements Extension {
    private Set<InjectionPoint> injectionPoints = new HashSet<>();
    
    public void processInjectionPoints(@Observes ProcessInjectionPoint<?, ?> pip) {
        InjectionPoint ip = pip.getInjectionPoint();

        if (ip.getAnnotated().isAnnotationPresent(ConfigProperty.class)) {
            if (isNotAProducedType(ip.getType())) {
                injectionPoints.add(ip);
            }
        }
    }
    
    public void registerConfigPropertyBeans(@Observes AfterBeanDiscovery abd, BeanManager bm) {
        for (InjectionPoint ip : injectionPoints) {
            abd.addBean(new ConfigPropertyBean<>(bm, (Class)ip.getType()));
        }
    }

    private boolean isNotAProducedType(Type ipType) {
        // Should be different from all types produced by Producers#XXXX
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
                && ipType != URL.class
                && ipType != Optional.class
                && ipType != Producer.class
                ;
    }
}
