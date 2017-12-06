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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleConfigManifestTest {
    
    @Test
    public void can_retrieve_entries() throws IOException {
        Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources("META-INF/microprofile-config.properties");

        ArrayList<URL> urlsList = Collections.list(urls);
        Assert.assertTrue(urlsList.size() > 0);
    }
}
