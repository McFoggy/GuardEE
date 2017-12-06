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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class CopyOnBuildAggregationConfigSource implements ConfigSource {
    private final Map<String, String> aggregated;
    public CopyOnBuildAggregationConfigSource(Collection<ConfigSource> sources) {
        LinkedList<ConfigSource> sourcesOrdered = new LinkedList<>(sources);
        Collections.reverse(sourcesOrdered);

        aggregated = new HashMap<>();
        
        sourcesOrdered.stream().map(ConfigSource::getProperties).forEach(aggregated::putAll);
    }
    
    @Override
    public Map<String, String> getProperties() {
        return aggregated;
    }

    @Override
    public String getValue(String propertyName) {
        return aggregated.get(propertyName);
    }

    @Override
    public String getName() {
        return "aggregated copy-on-build config source";
    }
}
