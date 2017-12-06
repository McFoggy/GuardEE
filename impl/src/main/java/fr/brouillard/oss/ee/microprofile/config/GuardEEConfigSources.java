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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.eclipse.microprofile.config.spi.ConfigSource;

import fr.brouillard.oss.ee.microprofile.config.sources.DelegateHashMapConfigSource;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GuardEEConfigSources {

    /**
     * Default value for non specified ordinal, see http://microprofile.io/project/eclipse/microprofile-config/spec/src/main/asciidoc/configsources.asciidoc
     */
    public static final int DEFAULT_CONFIG_SOURCE_ORDINAL = 100;

    public static Collection<ConfigSource> defaultConfigSources() {
    	Collection<ConfigSource> sources = new ArrayList<>();
    	
    	// Let's add first the sources found from standard manifest entries
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("META-INF/microprofile-config.properties");

            int i = 0;
            while (resources.hasMoreElements()) {
                URL url =  resources.nextElement();
                Properties p = new Properties();
                try (InputStream is = url.openStream()) {
                    p.load(is);
                }

                int ordinal = DEFAULT_CONFIG_SOURCE_ORDINAL;
                String ordinalString = p.getProperty(CONFIG_ORDINAL);
                if (ordinalString != null) {
                    try {
                        ordinal = Integer.parseInt(ordinalString);
                    } catch (NumberFormatException nfe) {}
                }
                sources.add(new DelegateHashMapConfigSource("URL-" + i, new HashMap(p), ordinal));
            }
        } catch (IOException e) {}
        
        // then add the global ones
        sources.add(SYSTEM_PROPERTY_CONFIG_SOURCE);
        sources.add(ENV_VARIABLE_CONFIG_SOURCE);

        return sources;
    }

	public static final String CONFIG_ORDINAL = "config_ordinal";
    
	private final static ConfigSource SYSTEM_PROPERTY_CONFIG_SOURCE = new DelegateHashMapConfigSource("System properties", new HashMap(System.getProperties()), 400);
    private final static ConfigSource ENV_VARIABLE_CONFIG_SOURCE = new DelegateHashMapConfigSource("Environment variables", System.getenv(), 300);

}
