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
package fr.brouillard.oss.ee.fault.tolerance.fallback;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.faulttolerance.FallbackHandler;

import fr.brouillard.oss.ee.fault.tolerance.impl.ExecutionContextImpl;

public class FallbackHandlerInvoker {
    @Inject
    Instance<FallbackHandler> fallbackHandlerBuilder;
    
    public Object invoke(InvocationContext context, Class<? extends FallbackHandler<?>> fhClass) {
        Instance<? extends FallbackHandler<?>> instance = fallbackHandlerBuilder.select(fhClass);
        
        if (instance.isUnsatisfied()) {
            throw new IllegalStateException("could not find a valid instance of " + fhClass.getName());
        }
        
        FallbackHandler<?> fallbackHandler = instance.get();
        return fallbackHandler.handle(new ExecutionContextImpl(context.getMethod(), context.getParameters()));
    }
}
