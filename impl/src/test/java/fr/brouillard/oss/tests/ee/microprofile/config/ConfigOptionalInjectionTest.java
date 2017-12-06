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

import java.util.Optional;

import javax.enterprise.context.Dependent;
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
import fr.brouillard.oss.tests.ee.microprofile.config.sources.SimpleConfigSource;

public class ConfigOptionalInjectionTest extends Arquillian {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        ConfigOptionalInjectionTest.class
                        , SimpleConfigSource.class
                        , InjectionOptionalJavaTypesBean.class
                )
                .addPackages(true, "fr.brouillard.oss.ee.microprofile.config", "fr.brouillard.oss.ee.microprofile.misc")
                .addAsServiceProvider(Extension.class, GuardEEConfigExtension.class)
                .addAsServiceProvider(ConfigProviderResolver.class, GuardEEConfigProviderResolver.class)
                .addAsServiceProvider(ConfigSource.class, SimpleConfigSource.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    BeanManager bm;

    @Test
    public void check_injections_of_simple_java_types() {
        InjectionOptionalJavaTypesBean bean = getBeanOfType(InjectionOptionalJavaTypesBean.class);

        Assert.assertNotNull(bean);

        Assert.assertNotNull(bean.stringProperty);
        Assert.assertNotNull(bean.booleanObjProperty);
        Assert.assertNotNull(bean.integerProperty);
        Assert.assertNotNull(bean.longObjProperty);
        Assert.assertNotNull(bean.floatObjProperty);
        Assert.assertNotNull(bean.doubleObjProperty);


        Assert.assertTrue(bean.stringProperty.isPresent());
        Assert.assertTrue(bean.booleanObjProperty.isPresent());
        Assert.assertTrue(bean.integerProperty.isPresent());
        Assert.assertTrue(bean.longObjProperty.isPresent());
        Assert.assertTrue(bean.floatObjProperty.isPresent());
        Assert.assertTrue(bean.doubleObjProperty.isPresent());
        
        Assert.assertEquals(bean.stringProperty.get(), "text");
        Assert.assertTrue(bean.booleanObjProperty.get());
        Assert.assertEquals(bean.integerProperty.get(), Integer.valueOf(5));
        Assert.assertEquals(bean.longObjProperty.get(), Long.valueOf(10l));
        Assert.assertEquals(bean.floatObjProperty.get(), Float.valueOf(10.5f));
        Assert.assertEquals(bean.doubleObjProperty.get(), Double.valueOf(11.5d));

        Assert.assertEquals(bean.stringPropertyWithDefaultValue.get(), "default");
        Assert.assertTrue(bean.booleanObjPropertyWithDefaultValue.get());
        Assert.assertEquals(bean.integerPropertyWithDefaultValue.get(), Integer.valueOf(1));
        Assert.assertEquals(bean.longObjPropertyWithDefaultValue.get(), Long.valueOf(2l));
        Assert.assertEquals(bean.floatObjPropertyWithDefaultValue.get(), Float.valueOf(2.1f));
        Assert.assertEquals(bean.doubleObjPropertyWithDefaultValue.get(), Double.valueOf(3.1415d));

        Assert.assertFalse(bean.stringPropertyNotExisting.isPresent());
        Assert.assertFalse(bean.booleanObjPropertyNotExisting.isPresent());
        Assert.assertFalse(bean.integerPropertyNotExisting.isPresent());
        Assert.assertFalse(bean.longObjPropertyNotExisting.isPresent());
        Assert.assertFalse(bean.floatObjPropertyNotExisting.isPresent());
        Assert.assertFalse(bean.doubleObjPropertyNotExisting.isPresent());
    }

    @SuppressWarnings("unchecked")
	private <T> T getBeanOfType(Class<T> beanClass) {
        Bean<T> bean = (Bean<T>) bm.resolve(bm.getBeans(beanClass));
        T beanInstance = bm.getContext(bean.getScope()).get(bean, bm.createCreationalContext(bean));

        return beanInstance;
    }

    @Dependent
    public static class InjectionOptionalJavaTypesBean {
        @Inject
        @ConfigProperty(name = "simple.string.property")
        private Optional<String> stringProperty;

        @Inject
        @ConfigProperty(name = "simple.boolean.property")
        private Optional<Boolean> booleanObjProperty;

        @Inject
        @ConfigProperty(name = "simple.int.property")
        private Optional<Integer> integerProperty;

        @Inject
        @ConfigProperty(name = "simple.long.property")
        private Optional<Long> longObjProperty;

        @Inject
        @ConfigProperty(name = "simple.float.property")
        private Optional<Float> floatObjProperty;

        @Inject
        @ConfigProperty(name = "simple.double.property")
        private Optional<Double> doubleObjProperty;

        @Inject
        @ConfigProperty(name = "simple.not.configured.string.property", defaultValue = "default")
        private Optional<String> stringPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.boolean.property", defaultValue = "true")
        private Optional<Boolean> booleanObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.int.property", defaultValue = "1")
        private Optional<Integer> integerPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.long.property", defaultValue = "2")
        private Optional<Long> longObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.float.property", defaultValue = "2.1")
        private Optional<Float> floatObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.double.property", defaultValue = "3.1415")
        private Optional<Double> doubleObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.string.property")
        private Optional<String> stringPropertyNotExisting;

        @Inject
        @ConfigProperty(name = "simple.not.configured.boolean.property")
        private Optional<Boolean> booleanObjPropertyNotExisting;

        @Inject
        @ConfigProperty(name = "simple.not.configured.int.property")
        private Optional<Integer> integerPropertyNotExisting;

        @Inject
        @ConfigProperty(name = "simple.not.configured.long.property")
        private Optional<Long> longObjPropertyNotExisting;

        @Inject
        @ConfigProperty(name = "simple.not.configured.float.property")
        private Optional<Float> floatObjPropertyNotExisting;

        @Inject
        @ConfigProperty(name = "simple.not.configured.double.property")
        private Optional<Double> doubleObjPropertyNotExisting;
    }
}
