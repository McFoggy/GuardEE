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

import java.util.HashMap;
import java.util.Map;

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
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.brouillard.oss.ee.microprofile.config.GuardEEConfigProviderResolver;
import fr.brouillard.oss.ee.microprofile.config.cdi.GuardEEConfigExtension;

public class ConfigInjectionTest extends Arquillian {
    @Deployment
    public static Archive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(ConfigInjectionTest.class)
                .addPackages(true, "fr.brouillard.oss.ee.microprofile.config", "fr.brouillard.oss.ee.microprofile.misc")
                .addAsServiceProvider(Extension.class, GuardEEConfigExtension.class)
                .addAsServiceProvider(ConfigProviderResolver.class, GuardEEConfigProviderResolver.class)
                .addAsServiceProvider(ConfigSource.class, SimpleConfigSource.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    @Inject
    BeanManager bm;
    
    @Test
    public void check_injections_of_java_types() {
        InjectionJavaTypesBean bean = getBeanOfType(InjectionJavaTypesBean.class);

        Assert.assertNotNull(bean);
        
        Assert.assertEquals(bean.stringProperty, "text");
        Assert.assertTrue(bean.booleanObjProperty);
        Assert.assertEquals(bean.integerProperty, Integer.valueOf(5));
        Assert.assertEquals(bean.longObjProperty, Long.valueOf(10l));
        Assert.assertEquals(bean.floatObjProperty, Float.valueOf(10.5f));
        Assert.assertEquals(bean.doubleObjProperty, Double.valueOf(11.5d));

        Assert.assertEquals(bean.stringPropertyWithDefaultValue, "default");
        Assert.assertTrue(bean.booleanObjPropertyWithDefaultValue);
        Assert.assertEquals(bean.integerPropertyWithDefaultValue, Integer.valueOf(1));
        Assert.assertEquals(bean.longObjPropertyWithDefaultValue, Long.valueOf(2l));
        Assert.assertEquals(bean.floatObjPropertyWithDefaultValue, Float.valueOf(2.1f));
        Assert.assertEquals(bean.doubleObjPropertyWithDefaultValue, Double.valueOf(3.1415d));

        Assert.assertNull(bean.stringPropertyNotExisting);
        Assert.assertNull(bean.booleanObjPropertyNotExisting);
        Assert.assertNull(bean.integerPropertyNotExisting);
        Assert.assertNull(bean.longObjPropertyNotExisting);
        Assert.assertNull(bean.floatObjPropertyNotExisting);
        Assert.assertNull(bean.doubleObjPropertyNotExisting);
    }
    

    private <T> T getBeanOfType(Class<T> beanClass) {
        Bean<T> bean = (Bean<T>) bm.resolve(bm.getBeans(beanClass));
        T beanInstance = bm.getContext(bean.getScope()).get(bean, bm.createCreationalContext(bean));
        
        return beanInstance;
    }
    
    @Dependent
    public static class InjectionJavaTypesBean {
        @Inject
        @ConfigProperty(name="simple.string.property")
        private String stringProperty;

        @Inject
        @ConfigProperty(name="simple.boolean.property")
        private Boolean booleanObjProperty;

        @Inject
        @ConfigProperty(name="simple.int.property")
        private Integer integerProperty;

        @Inject
        @ConfigProperty(name="simple.long.property")
        private Long longObjProperty;

        @Inject
        @ConfigProperty(name="simple.float.property")
        private Float floatObjProperty;

        @Inject
        @ConfigProperty(name="simple.double.property")
        private Double doubleObjProperty;

        @Inject
        @ConfigProperty(name="simple.not.configured.string.property", defaultValue = "default")
        private String stringPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name="simple.not.configured.boolean.property", defaultValue = "true")
        private Boolean booleanObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name="simple.not.configured.int.property", defaultValue = "1")
        private Integer integerPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name="simple.not.configured.long.property", defaultValue = "2")
        private Long longObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name="simple.not.configured.float.property", defaultValue = "2.1")
        private Float floatObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name="simple.not.configured.double.property", defaultValue = "3.1415")
        private Double doubleObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name="simple.not.configured.string.property")
        private String stringPropertyNotExisting;

        @Inject
        @ConfigProperty(name="simple.not.configured.boolean.property")
        private Boolean booleanObjPropertyNotExisting;

        @Inject
        @ConfigProperty(name="simple.not.configured.int.property")
        private Integer integerPropertyNotExisting;

        @Inject
        @ConfigProperty(name="simple.not.configured.long.property")
        private Long longObjPropertyNotExisting;

        @Inject
        @ConfigProperty(name="simple.not.configured.float.property")
        private Float floatObjPropertyNotExisting;

        @Inject
        @ConfigProperty(name="simple.not.configured.double.property")
        private Double doubleObjPropertyNotExisting;
    }

    public static class SimpleConfigSource implements ConfigSource {

        private Map<String, String> properties;

        public SimpleConfigSource() {
            properties = new HashMap<>();
            properties.put("simple.string.property", "text");
            properties.put("simple.boolean.property", "true");
            properties.put("simple.int.property", "5");
            properties.put("simple.long.property", "10");
            properties.put("simple.float.property", "10.5");
            properties.put("simple.double.property", "11.5");
        }

        @Override
        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public String getValue(String propertyName) {
            return properties.get(propertyName);
        }

        @Override
        public String getName() {
            return this.getClass().getName();
        }
    }    
}
