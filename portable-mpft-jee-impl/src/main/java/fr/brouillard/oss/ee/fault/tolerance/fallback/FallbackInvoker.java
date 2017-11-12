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
package fr.brouillard.oss.ee.fault.tolerance.fallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceDefinitionException;

import fr.brouillard.oss.ee.fault.tolerance.config.AnnotationFinder;
import fr.brouillard.oss.ee.fault.tolerance.impl.Invoker;
import fr.brouillard.oss.ee.fault.tolerance.impl.InvokerChain;

public class FallbackInvoker implements Invoker {
    @Inject
    FallbackHandlerInvoker fhInvoker;
    
    @Override
    public Object invoke(InvocationContext context, InvokerChain chain) throws Exception {
        try {
            return chain.invoke(context);
        } catch (Exception ex) {
            Optional<Fallback> optFallback = AnnotationFinder.find(context, Fallback.class);
            
            if (optFallback.isPresent()) {
                Fallback annotationFallback = optFallback.get();
                if (Fallback.DEFAULT.class.getName().equals(annotationFallback.value().getName())) {
                    String fallbackMethodName = annotationFallback.fallbackMethod();

                    try {
                        Method fallbackMethod = context.getTarget().getClass().getMethod(fallbackMethodName, context.getMethod().getParameterTypes());
                        return  fallbackMethod.invoke(context.getTarget(), context.getParameters());
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new FaultToleranceDefinitionException(e);
                    }
                } else {
                    // Invocation shall be done by the class defined on the Fallback annotation
                    return fhInvoker.invoke(context, annotationFallback.value());
                }
            }

            throw new FaultToleranceDefinitionException("missing annotation @Fallback");
        }
    }
}
