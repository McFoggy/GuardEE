package fr.brouillard.oss.ee.fault.tolerance.interceptors;

import javax.annotation.Priority;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.faulttolerance.Retry;

import fr.brouillard.oss.ee.fault.tolerance.model.InvocationConfiguration;
import fr.brouillard.oss.ee.fault.tolerance.Configurator;
import org.eclipse.microprofile.faulttolerance.Timeout;

@Retry
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_AFTER)
public class RetryInterceptor {
    private final Bean<?> bean;
    private final FaultToleranceAnnotationsResolver annotationResolver;
    private final Configurator cfg;
    private final FaultToleranceInvoker invoker;

    @Inject
    private RetryInterceptor(@Intercepted Bean<?> bean, FaultToleranceAnnotationsResolver annotationResolver, Configurator cfg, FaultToleranceInvoker invoker) {
        this.bean = bean;
        this.annotationResolver = annotationResolver;
        this.cfg = cfg;
        this.invoker = invoker;
    }

    @AroundInvoke
    public Object executeFaultTolerance(InvocationContext invocationContext) throws Exception {
        FaultToleranceAnnotationsResolver.Of<Retry> retry = annotationResolver.retry(bean.getBeanClass(), invocationContext.getMethod());
        FaultToleranceAnnotationsResolver.Of<Timeout> timeout = annotationResolver.timeout(bean.getBeanClass(), invocationContext.getMethod());

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
