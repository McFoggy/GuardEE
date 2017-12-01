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

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;

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

public class ConfigSimpleInjectionTest extends Arquillian {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        ConfigSimpleInjectionTest.class
                        , SimpleConfigSource.class
                        , InjectionSimpleJavaTypesBean.class
                        , InjectionURLJavaTypeBean.class
                        , InjectionTimeJavaTypesBean.class
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
        InjectionSimpleJavaTypesBean bean = getBeanOfType(InjectionSimpleJavaTypesBean.class);

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

    @Test
    public void check_injections_of_time_java_types() {
        InjectionTimeJavaTypesBean bean = getBeanOfType(InjectionTimeJavaTypesBean.class);

        Assert.assertNotNull(bean);

        long expectedDurationInSeconds = (2 * 24 * 60 * 60l) + (3 * 60 * 60l) + (4 * 60l);      // 2 days 3 hours 4 minutes
        Assert.assertEquals(bean.durationObjProperty.getSeconds(), expectedDurationInSeconds);
        Assert.assertEquals(bean.localtimeObjProperty.toString(), "10:15:30");
        Assert.assertEquals(bean.localdateObjProperty.toString(), "2017-11-29");
        Assert.assertEquals(bean.localdatetimeObjProperty.toString(), "2017-11-29T10:15:30");
        Assert.assertEquals(bean.offsetdatetimeObjProperty.toString(), "2017-11-29T10:15:30+01:00");
        Assert.assertEquals(bean.offsettimeObjProperty.toString(), "10:15:30+01:00");
        Assert.assertEquals(bean.instantObjProperty.toString(), "2017-11-29T09:15:30Z");
    }

    @Test
    public void check_injections_of_url_java_type() {
        InjectionURLJavaTypeBean bean = getBeanOfType(InjectionURLJavaTypeBean.class);

        Assert.assertNotNull(bean);

        Assert.assertEquals(bean.urlObjProperty.toString(), "http://microprofile.io");
    }

    @SuppressWarnings("unchecked")
	private <T> T getBeanOfType(Class<T> beanClass) {
        Bean<T> bean = (Bean<T>) bm.resolve(bm.getBeans(beanClass));
        T beanInstance = bm.getContext(bean.getScope()).get(bean, bm.createCreationalContext(bean));

        return beanInstance;
    }

    @Dependent
    public static class InjectionSimpleJavaTypesBean {
        @Inject
        @ConfigProperty(name = "simple.string.property")
        private String stringProperty;

        @Inject
        @ConfigProperty(name = "simple.boolean.property")
        private Boolean booleanObjProperty;

        @Inject
        @ConfigProperty(name = "simple.int.property")
        private Integer integerProperty;

        @Inject
        @ConfigProperty(name = "simple.long.property")
        private Long longObjProperty;

        @Inject
        @ConfigProperty(name = "simple.float.property")
        private Float floatObjProperty;

        @Inject
        @ConfigProperty(name = "simple.double.property")
        private Double doubleObjProperty;

        @Inject
        @ConfigProperty(name = "simple.not.configured.string.property", defaultValue = "default")
        private String stringPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.boolean.property", defaultValue = "true")
        private Boolean booleanObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.int.property", defaultValue = "1")
        private Integer integerPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.long.property", defaultValue = "2")
        private Long longObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.float.property", defaultValue = "2.1")
        private Float floatObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.double.property", defaultValue = "3.1415")
        private Double doubleObjPropertyWithDefaultValue;

        @Inject
        @ConfigProperty(name = "simple.not.configured.string.property")
        private String stringPropertyNotExisting;

        @Inject
        @ConfigProperty(name = "simple.not.configured.boolean.property")
        private Boolean booleanObjPropertyNotExisting;

        @Inject
        @ConfigProperty(name = "simple.not.configured.int.property")
        private Integer integerPropertyNotExisting;

        @Inject
        @ConfigProperty(name = "simple.not.configured.long.property")
        private Long longObjPropertyNotExisting;

        @Inject
        @ConfigProperty(name = "simple.not.configured.float.property")
        private Float floatObjPropertyNotExisting;

        @Inject
        @ConfigProperty(name = "simple.not.configured.double.property")
        private Double doubleObjPropertyNotExisting;
    }


    @Dependent
    public static class InjectionURLJavaTypeBean {
        @Inject
        @ConfigProperty(name = "simple.url.property")
        private URL urlObjProperty;
    }

    @Dependent
    public static class InjectionTimeJavaTypesBean {
        @Inject
        @ConfigProperty(name = "simple.duration.property")
        private Duration durationObjProperty;
        @Inject
        @ConfigProperty(name = "simple.localtime.property")
        private LocalTime localtimeObjProperty;
        @Inject
        @ConfigProperty(name = "simple.localdate.property")
        private LocalDate localdateObjProperty;
        @Inject
        @ConfigProperty(name = "simple.localdatetime.property")
        private LocalDateTime localdatetimeObjProperty;
        @Inject
        @ConfigProperty(name = "simple.offsetdatetime.property")
        private OffsetDateTime offsetdatetimeObjProperty;
        @Inject
        @ConfigProperty(name = "simple.offsettime.property")
        private OffsetTime offsettimeObjProperty;
        @Inject
        @ConfigProperty(name = "simple.instant.property")
        private Instant instantObjProperty;
    }
}
