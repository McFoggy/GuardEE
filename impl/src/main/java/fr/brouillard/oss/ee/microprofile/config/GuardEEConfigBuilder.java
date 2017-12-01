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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ServiceLoader;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigBuilder;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;
import org.eclipse.microprofile.config.spi.Converter;

public class GuardEEConfigBuilder implements ConfigBuilder {
    private final ArrayList<ConfigSource> sources;
    private final ArrayList<Converter<?>> converters;
    private boolean includeDefaultSources = false;
    private boolean includeDiscoveredSources = false;
    private boolean includeDiscoveredConverters = false;
    private ClassLoader forClassLoader;

    public GuardEEConfigBuilder() {
        this.sources = new ArrayList<ConfigSource>();
        this.converters = new ArrayList<Converter<?>>();
        this.forClassLoader = Thread.currentThread().getContextClassLoader();
    } 
    
    @Override
    public ConfigBuilder addDefaultSources() {
        this.includeDefaultSources = true;
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredSources() {
        this.includeDiscoveredSources = true;
        return this;
    }

    @Override
    public ConfigBuilder addDiscoveredConverters() {
        this.includeDiscoveredConverters = true;
        return this;
    }

    @Override
    public ConfigBuilder forClassLoader(ClassLoader loader) {
        this.forClassLoader = loader;
        return this;
    }

    @Override
    public ConfigBuilder withSources(ConfigSource... sources) {
        this.sources.addAll(Arrays.asList(sources));
        return this;
    }

    @Override
    public ConfigBuilder withConverters(Converter<?>... converters) {
        this.converters.addAll(Arrays.asList(converters));
        return this;
    }

    @Override
    public Config build() {
        if (includeDefaultSources) {
            sources.addAll(GuardEEConfigSources.defaultConfigSources());
        }
        if (includeDiscoveredSources) {
            addServiceLoaderConfigSource();
            addServiceLoaderConfigSourceProvider(forClassLoader);
        }
        if (includeDiscoveredConverters) {
            addCustomConverters();
        }
        return new GuardEEConfig(sources, converters);
    }

    @SuppressWarnings("rawtypes")
	private void addCustomConverters() {
        ServiceLoader<Converter> converters = ServiceLoader.load(Converter.class);
        for (Converter converter : converters) {
            this.converters.add(converter);
        }
    }

    private void addServiceLoaderConfigSource() {
        ServiceLoader<ConfigSource> sources = ServiceLoader.load(ConfigSource.class);
        for (ConfigSource cs : sources) {
            this.sources.add(cs);
        }
    }

    private void addServiceLoaderConfigSourceProvider(ClassLoader cl) {
        ServiceLoader<ConfigSourceProvider> providers = ServiceLoader.load(ConfigSourceProvider.class);
        for (ConfigSourceProvider provider : providers) {
            Iterable<ConfigSource> configSources = provider.getConfigSources(cl);
            for (ConfigSource cs : configSources) {
                this.sources.add(cs);
            }
        }
    }
}
