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

import javax.enterprise.util.AnnotationLiteral;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@SuppressWarnings("serial")
public class ConfigPropertyAnnotationLiteral extends AnnotationLiteral<ConfigProperty> implements ConfigProperty {
    @Override
    public String name() {
        return "";
    }

    @Override
    public String defaultValue() {
        return "";
    }
}
