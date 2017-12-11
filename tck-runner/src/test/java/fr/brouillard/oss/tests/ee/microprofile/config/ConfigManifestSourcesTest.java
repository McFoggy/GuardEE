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

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
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

import fr.brouillard.oss.ee.microprofile.config.GuardEEConfigProviderResolver;
import fr.brouillard.oss.ee.microprofile.config.cdi.GuardEEConfigExtension;

public class ConfigManifestSourcesTest extends Arquillian {

    @Deployment
    public static WebArchive deploy() {
        JavaArchive testJar = ShrinkWrap
                .create(JavaArchive.class, "cdiOptionalInjectionTest.jar")
                .addClasses(ConfigManifestSourcesTest.class, SimpleConfigManifestBean.class)
                .addAsManifestResource(new StringAsset("my.int=1234\nmy.string=hello"),
                        "microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .as(JavaArchive.class);

        WebArchive war = ShrinkWrap
                .create(WebArchive.class, "cdiOptionalInjectionTest.war")
                .addAsLibrary(testJar)
                ;
        
//        war.as(ZipExporter.class).exportTo(new File("d:/test.war"), true);
        return war;
    }

    @Inject
    BeanManager bm;

    @SuppressWarnings("unchecked")
	private <T> T getBeanOfType(Class<T> beanClass) {
        Bean<T> bean = (Bean<T>) bm.resolve(bm.getBeans(beanClass));
        T beanInstance = bm.getContext(bean.getScope()).get(bean, bm.createCreationalContext(bean));

        return beanInstance;
    }
    
    @Test
    public void check_injection_of_manifest_source_occured() {
    	SimpleConfigManifestBean bean = getBeanOfType(SimpleConfigManifestBean.class);
    	
    	Assert.assertEquals(bean.amount.intValue(), 1234);
    	Assert.assertEquals(bean.message, "hello");
    }
    
    public static class SimpleConfigManifestBean {
    	@Inject
    	@ConfigProperty(name="my.int")
    	Integer amount;
    	
    	@Inject
    	@ConfigProperty(name="my.string")
    	String message;
    }

}
