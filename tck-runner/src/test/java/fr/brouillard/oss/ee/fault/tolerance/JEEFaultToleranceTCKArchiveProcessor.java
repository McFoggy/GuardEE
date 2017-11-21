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
package fr.brouillard.oss.ee.fault.tolerance;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import java.io.File;

public class JEEFaultToleranceTCKArchiveProcessor implements ApplicationArchiveProcessor {

    public static final String GUARDEE_VERSION = "guardee.version";

    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        if (applicationArchive instanceof WebArchive) {
            WebArchive war = (WebArchive) applicationArchive;
            String implVersion = System.getProperty(GUARDEE_VERSION);
            if (implVersion != null && implVersion.trim().length() > 0) {
                String gav = String.format("fr.brouillard.oss.jee:guardee-impl:%s", implVersion);

                System.out.println("adding " + gav + " dependencies to " + war.getName());

                File[] jeeImplAndDeps = Maven.resolver().resolve(gav).withTransitivity().asFile();
                war.addAsLibraries(jeeImplAndDeps);
                
                if (Boolean.getBoolean("arquillian.archive.add.beans.xml")) {
                    war.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
                }

                String dumpPath = System.getProperty("arquillian.archive.dump");
                if (dumpPath != null) {
                    war.as(ZipExporter.class).exportTo(new File(dumpPath + System.currentTimeMillis() + "-" + war.getName()), true);
                }
            } else {
                throw new RuntimeException("could not find suitable version of project using System property: " + GUARDEE_VERSION);
            }
        }
    }
}
