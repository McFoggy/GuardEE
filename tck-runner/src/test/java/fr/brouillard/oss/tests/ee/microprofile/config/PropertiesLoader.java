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
package fr.brouillard.oss.tests.ee.microprofile.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class PropertiesLoader {
    private final List<Properties> properties;
    public PropertiesLoader(String resource) {
        this(PropertiesLoader.class.getClassLoader(), resource);
    }
    
    public PropertiesLoader(ClassLoader loader, String resource) {
        properties = new ArrayList<>();
        
        try {
            Enumeration<URL> urls = loader.getResources(resource);
            while (urls.hasMoreElements()) {
                URL u = urls.nextElement();
                try(InputStream is = u.openStream()) {
                    Properties p = new Properties();
                    p.load(is);
                    properties.add(p);
                }
            }
        } catch (IOException e) {}
    }
    
    public String getValue(String key) {
        return properties.stream()
                .filter(p -> p.containsKey(key))
                .findFirst()
                .map(p -> p.getProperty(key))
                .orElse(null);
    }
}
