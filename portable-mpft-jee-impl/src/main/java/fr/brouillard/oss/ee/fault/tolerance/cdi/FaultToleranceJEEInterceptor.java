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
package fr.brouillard.oss.ee.fault.tolerance.cdi;

import fr.brouillard.oss.ee.fault.tolerance.config.Configurator;
import fr.brouillard.oss.ee.fault.tolerance.impl.FaultToleranceInvoker;
import fr.brouillard.oss.ee.fault.tolerance.model.InvocationConfiguration;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import javax.annotation.Priority;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@FaultToleranceJEE
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE - 10)      // Just before libraries       
public class FaultToleranceJEEInterceptor {
    private final Bean<?> bean;
    private final AnnotationsResolver annotationResolver;
    private final Configurator cfg;
    private final FaultToleranceInvoker invoker;

    @Inject
    public FaultToleranceJEEInterceptor(@Intercepted Bean<?> bean, AnnotationsResolver annotationResolver, Configurator cfg, FaultToleranceInvoker invoker) {
        this.bean = bean;
        this.annotationResolver = annotationResolver;
        this.cfg = cfg;
        this.invoker = invoker;
    }

    @AroundInvoke
    public Object executeFaultTolerance(InvocationContext invocationContext) throws Exception {
        AnnotationsResolver.Of<Retry> retry = annotationResolver.retry(bean.getBeanClass(), invocationContext.getMethod());
        AnnotationsResolver.Of<Timeout> timeout = annotationResolver.timeout(bean.getBeanClass(), invocationContext.getMethod());

        InvocationConfiguration ic = InvocationConfiguration.any();

        if (retry.isPresent()) {
            ic = cfg.retry(retry.name(), ic.with(retry.annotation()));
        }

        if (timeout.isPresent()) {
            ic = cfg.timeout(timeout.name(), ic.with(timeout.annotation()));
        }

        return invoker.invoke(ic, invocationContext);
    }
}
