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
package fr.brouillard.oss.tests.ee.microprofile.config.sources;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class SimpleConfigSource implements ConfigSource {

    private Map<String, String> properties;

    public SimpleConfigSource() {
        properties = new HashMap<>();
        properties.put("simple.string.property", "text");
        properties.put("simple.boolean.property", "true");
        properties.put("simple.int.property", "5");
        properties.put("simple.long.property", "10");
        properties.put("simple.float.property", "10.5");
        properties.put("simple.double.property", "11.5");
        properties.put("simple.duration.property", "P2DT3H4M");
        properties.put("simple.localtime.property", "10:15:30");
        properties.put("simple.localdate.property", "2017-11-29");
        properties.put("simple.localdatetime.property", "2017-11-29T10:15:30");
        properties.put("simple.offsetdatetime.property", "2017-11-29T10:15:30+01:00");
        properties.put("simple.offsettime.property", "10:15:30+01:00");
        properties.put("simple.instant.property", "2017-11-29T09:15:30Z");
        properties.put("simple.url.property", "http://microprofile.io");
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public String getValue(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }
}
