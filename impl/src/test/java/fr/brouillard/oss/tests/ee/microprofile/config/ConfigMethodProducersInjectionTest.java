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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.brouillard.oss.ee.microprofile.config.GuardEEConfigProviderResolver;
import fr.brouillard.oss.ee.microprofile.config.cdi.GuardEEConfigExtension;
import fr.brouillard.oss.tests.ee.microprofile.config.sources.AnimalsConfigSource;

public class ConfigMethodProducersInjectionTest extends Arquillian {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        ConfigMethodProducersInjectionTest.class
                        , AnimalsConfigSource.class
                        , Dog.class
                        , Cat.class
                        , House.class
                )
                .addPackages(true, "fr.brouillard.oss.ee.microprofile.config", "fr.brouillard.oss.ee.microprofile.misc")
                .addAsServiceProvider(Extension.class, GuardEEConfigExtension.class)
                .addAsServiceProvider(ConfigProviderResolver.class, GuardEEConfigProviderResolver.class)
                .addAsServiceProvider(ConfigSource.class, AnimalsConfigSource.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    BeanManager bm;

    @Test
    public void check_injections() {
        House inHouse = getBeanOfType(House.class);
        
        Assert.assertNotNull(inHouse);
        Assert.assertNotNull(inHouse.rahan, "Dog rahan could not be injected");
        Assert.assertNotNull(inHouse.prolo, "Dog prolo could not be injected");
        Assert.assertNotNull(inHouse.hercule, "Cat hercule could not be injected");
        Assert.assertNotNull(inHouse.garfield, "Cat garfield could not be injected");
        
        Assert.assertEquals(inHouse.rahan.getName(), "rahan");
        Assert.assertEquals(inHouse.prolo.getName(), "prolo");
        Assert.assertEquals(inHouse.hercule.getName(), "hercule");
        Assert.assertEquals(inHouse.garfield.getName(), "garfield");
    }

    @SuppressWarnings("unchecked")
	private <T> T getBeanOfType(Class<T> beanClass) {
        Bean<T> bean = (Bean<T>) bm.resolve(bm.getBeans(beanClass));
        T beanInstance = bm.getContext(bean.getScope()).get(bean, bm.createCreationalContext(bean));

        return beanInstance;
    }
    
    @ApplicationScoped
    public static class House {
        @Inject @ConfigProperty(name = "dog.rahan")
        Dog rahan;

        @Inject @ConfigProperty(name = "dog.prolo")
        Dog prolo;
        
        @Inject @ConfigProperty(name = "cat.hercule")
        Cat hercule;

        @Inject @ConfigProperty(name = "cat.garfield")
        Cat garfield;
    }

    public static class Dog {
        private String name;

        public Dog(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    
    public static class Cat {
        private String name;

        public Cat(String name) {
            this.name = name;
        }

        public static Cat valueOf(String name) {
            return new Cat(name);
        }

        public String getName() {
            return name;
        }
    }
}
