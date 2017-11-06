/**
 * Copyright © 2017 Matthieu Brouillard [http://oss.brouillard.fr/portable-mpft-jee] (matthieu@brouillard.fr)
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
package fr.brouillard.oss.ee.fault.tolerance.config;

import fr.brouillard.oss.ee.fault.tolerance.model.InvocationConfiguration;
import org.eclipse.microprofile.faulttolerance.Timeout;

public class Configurator {
    /**
     * Enhances given found retry configuration, with dynamic configuration if any
     * @param name the name of the retry configuration for which additional settings are being retrieved
     * @param baseConfiguration a base configuration to modify
     * @return the configuration to use
     */
    public InvocationConfiguration retry(String name, InvocationConfiguration baseConfiguration) {
        return baseConfiguration;
    }

    /**
     * Enhances given found retry configuration, with dynamic configuration if any
     * @param name the name of the timeout configuration for which additional settings are being retrieved
     * @param baseConfiguration a base configuration to modify
     * @return the configuration to use
     */
    public InvocationConfiguration timeout(String name, InvocationConfiguration baseConfiguration) {
        return baseConfiguration;
    }
}
