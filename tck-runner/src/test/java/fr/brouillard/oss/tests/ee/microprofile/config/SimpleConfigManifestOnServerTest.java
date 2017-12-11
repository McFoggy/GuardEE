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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SimpleConfigManifestOnServerTest extends Arquillian {
    @Deployment
    public static WebArchive deploy() {
        JavaArchive loaderJar = ShrinkWrap
                .create(JavaArchive.class, "loader.jar")
                .addClass(PropertiesLoader.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .as(JavaArchive.class);
        JavaArchive resourceOneJar = ShrinkWrap
                .create(JavaArchive.class, "resourceOne.jar")
                .addAsManifestResource(new StringAsset("my.int=1234"),
                        "microprofile-config.properties")
                .as(JavaArchive.class);
        JavaArchive resourceTwoJar = ShrinkWrap
                .create(JavaArchive.class, "resourceTwo.jar")
                .addAsManifestResource(new StringAsset("my.string=hello"),
                        "microprofile-config.properties")
                .as(JavaArchive.class);

        WebArchive war = ShrinkWrap
                .create(WebArchive.class, "cdiOptionalInjectionTest.war")
                .addAsLibraries(resourceOneJar, resourceTwoJar, loaderJar)
                ;

//        war.as(ZipExporter.class).exportTo(new File("d:/test.war"), true);
        return war;
    }
    
    @Test
    public void can_retrieve_entries_using_tccl() throws IOException {
        String metaInfStandard = "META-INF/microprofile-config.properties";
        PropertiesLoader pl = new PropertiesLoader(Thread.currentThread().getContextClassLoader(), metaInfStandard);

        Assert.assertEquals(pl.getValue("my.int"), "1234");
        Assert.assertEquals(pl.getValue("my.string"), "hello");
    }
    
    @Test
    public void can_retrieve_entries_using_default_classloader() throws IOException {
        String metaInfStandard = "META-INF/microprofile-config.properties";
        PropertiesLoader pl = new PropertiesLoader(metaInfStandard);

        Assert.assertEquals(pl.getValue("my.int"), "1234");
        Assert.assertEquals(pl.getValue("my.string"), "hello");
    }
}
