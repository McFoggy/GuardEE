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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class GuardEEConfigSources {
    public static Collection<ConfigSource> defaultConfigSources() {
        return DEFAULTS;
    }
    
    private final static Collection<ConfigSource> DEFAULTS = new ArrayList<>();

    public static final String CONFIG_ORDINAL = "config_ordinal";
    
    private final static ConfigSource SYSTEM_PROPERTY_CONFIG_SOURCE = new DelegateHashMapConfigSource("System properties", new HashMap(System.getProperties()), 400);
    private final static ConfigSource ENV_VARIABLE_CONFIG_SOURCE = new DelegateHashMapConfigSource("Environment variables", System.getenv(), 300);
    
    private static class DelegateHashMapConfigSource implements ConfigSource {
        private String name;
        private Map<String, String> delegate;
        private int defaultOrdinal;

        private DelegateHashMapConfigSource(String name, Map<String, String> delegate, int defaultOrdinal) {
            this.name = name;
            this.delegate = delegate;
            this.defaultOrdinal = defaultOrdinal;
        }

        @Override
        public Map<String, String> getProperties() {
            return delegate;
        }

        @Override
        public String getValue(String propertyName) {
            return delegate.get(propertyName);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getOrdinal() {
            try {
                return Integer.decode(delegate.get(CONFIG_ORDINAL));
            } catch (Exception ex) {
                return defaultOrdinal;
            }
        }
    }
    
    static {
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(" META-INF/microprofile-config.properties");

            int i = 0;
            while (resources.hasMoreElements()) {
                URL url =  resources.nextElement();
                Properties p = new Properties();
                try (InputStream is = url.openStream()) {
                    p.load(is);
                }

                DEFAULTS.add(new DelegateHashMapConfigSource("URL-" + i, new HashMap(p), 100));
            }
        } catch (IOException e) {
        }
        DEFAULTS.add(SYSTEM_PROPERTY_CONFIG_SOURCE);
        DEFAULTS.add(ENV_VARIABLE_CONFIG_SOURCE);
    }
}
