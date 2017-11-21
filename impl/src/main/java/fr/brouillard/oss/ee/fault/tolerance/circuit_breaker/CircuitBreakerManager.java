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
package fr.brouillard.oss.ee.fault.tolerance.circuit_breaker;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

import fr.brouillard.oss.ee.fault.tolerance.impl.ExecutionContextImpl;
import fr.brouillard.oss.ee.fault.tolerance.misc.Annotations;

@ApplicationScoped
public class CircuitBreakerManager {
    private ConcurrentMap<String, CircuitBreakerHandler> handlerByMethod;

    private final static CircuitBreakerHandler ALWAYS_CLOSED = new CircuitBreakerHandlerImpl(new Class[0], 0, 0, 1.0, 1);

    @PostConstruct
    public void initialize() {
        handlerByMethod = new ConcurrentHashMap<>();
    }

    CircuitBreakerHandler forName(String name) {
        return handlerByMethod.get(name);
    }

    public CircuitBreakerHandler forContext(ExecutionContextImpl executionContext) {
        String name = name(executionContext.getMethod());

        CircuitBreakerHandler handler = handlerByMethod.get(name);

        if (handler == null) {
            Optional<CircuitBreaker> cb = Annotations.find(executionContext.getMethod(), CircuitBreaker.class);
            handler = cb.map(this::createHandler).orElse(ALWAYS_CLOSED);
            handlerByMethod.putIfAbsent(name, handler);
        }

        return handler;
    }

    private CircuitBreakerHandler createHandler(CircuitBreaker circuitBreaker) {
        return new CircuitBreakerHandlerImpl(circuitBreaker);
    }

    private String name(Method method) {
        return String.format("%s#%s", method.getDeclaringClass().getName(), method.getName());
    }

}
