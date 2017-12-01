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

import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

public class GuardEEConfigProviderResolver extends ConfigProviderResolver {
    private final Map<ClassLoader, Config> registeredConfigurations;
    private Config defaultConfig;

    public GuardEEConfigProviderResolver() {
        registeredConfigurations = new WeakHashMap<>();
    }
    
    @Override
    public Config getConfig() {
        return getConfig(Thread.currentThread().getContextClassLoader());
    }

    @Override
    public Config getConfig(ClassLoader loader) {
        return registeredConfigurations.computeIfAbsent(loader, this::buildDefaultConfig);
    }

    @Override
    public ConfigBuilder getBuilder() {
        GuardEEConfigBuilder builder = new GuardEEConfigBuilder();
        builder.withConverters(GuardEEConfigConverters.defaultConverters());
        return builder;
    }

    @Override
    public void registerConfig(Config config, ClassLoader classLoader) {
        if (classLoader != null) {
            registeredConfigurations.put(classLoader, config);
        } else {
            defaultConfig = config;
        }
    }

    @Override
    public void releaseConfig(Config config) {
        if (defaultConfig == config) {
            defaultConfig = null;
        } else {
            for (Iterator<Map.Entry<ClassLoader, Config>> it = registeredConfigurations.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<ClassLoader, Config> entry = it.next();
                if (entry.getValue() == config) {
                    it.remove();
                }
            }
        }
    }
    
    private Config buildDefaultConfig(ClassLoader loader) {
        return getBuilder()
                .forClassLoader(loader)
                .addDefaultSources()
                .addDiscoveredSources()
                .addDiscoveredConverters()
                .build();
    }
}
