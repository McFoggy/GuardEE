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
package fr.brouillard.oss.ee.fault.tolerance.circuit_breaker;

import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

import fr.brouillard.oss.ee.fault.tolerance.impl.ExecutionContextImpl;
import fr.brouillard.oss.ee.fault.tolerance.impl.Invoker;
import fr.brouillard.oss.ee.fault.tolerance.impl.InvokerChain;

public class CircuitBreakerInvoker implements Invoker {
    private final CircuitBreakerManager circuitBreakerManager;

    @Inject
    public CircuitBreakerInvoker(CircuitBreakerManager cbm) {
        this.circuitBreakerManager = cbm;
    }
    
    @Override
    public Object invoke(InvocationContext context, InvokerChain chain) throws Exception {
        ExecutionContextImpl executionContext = new ExecutionContextImpl(context.getMethod(), context.getParameters());
        CircuitBreakerHandlerImpl circuitBreaker = (CircuitBreakerHandlerImpl) circuitBreakerManager.forContext(executionContext);
        
        circuitBreaker.enter();
        try {
            Object result = chain.invoke(context);
            circuitBreaker.success();
            return result;
        } catch (Exception problem) {
            throw circuitBreaker.onFailure(problem);
        }
    }
}
