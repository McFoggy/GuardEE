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
package fr.brouillard.oss.ee.microprofile.config.sources;

import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class DelegateHashMapConfigSource implements ConfigSource {
    private String name;
    private Map<String, String> delegate;
    private int defaultOrdinal;

    public DelegateHashMapConfigSource(String name, Map<String, String> delegate, int defaultOrdinal) {
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
