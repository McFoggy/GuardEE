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

import fr.brouillard.oss.ee.fault.tolerance.circuit_breaker.CircuitBreakerInvoker;
import fr.brouillard.oss.ee.fault.tolerance.config.AnnotationFinder;
import fr.brouillard.oss.ee.fault.tolerance.impl.Chains;
import fr.brouillard.oss.ee.fault.tolerance.impl.InvokerChain;
import fr.brouillard.oss.ee.fault.tolerance.retry.RetryInvoker;
import fr.brouillard.oss.ee.fault.tolerance.timeout.TimeoutInvoker;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@FaultToleranceJEE
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE - 10)      // Just before libraries       
public class FaultToleranceJEEInterceptor {
    private TimeoutInvoker timeoutInvoker;
    private CircuitBreakerInvoker circuitBreakerInvoker;
    private RetryInvoker retryInvoker;

    @Inject
    public FaultToleranceJEEInterceptor(TimeoutInvoker ti, CircuitBreakerInvoker cbi, RetryInvoker ri) {
        this.timeoutInvoker = ti;
        this.circuitBreakerInvoker = cbi;
        this.retryInvoker = ri;
    }

    @AroundInvoke
    public Object executeFaultTolerance(InvocationContext invocationContext) throws Exception {
        InvokerChain chain = Chains.end();
        
        if (AnnotationFinder.find(invocationContext, Timeout.class).isPresent()) {
            chain = Chains.decorate(timeoutInvoker, chain);
        }
        if (AnnotationFinder.find(invocationContext, CircuitBreaker.class).isPresent()) {
            chain = Chains.decorate(circuitBreakerInvoker, chain);
        }
        if (AnnotationFinder.find(invocationContext, Retry.class).isPresent()) {
            chain = Chains.decorate(retryInvoker, chain);
        }
        
        return chain.invoke(invocationContext);
    }
}
