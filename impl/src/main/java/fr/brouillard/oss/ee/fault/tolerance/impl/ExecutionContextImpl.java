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
package fr.brouillard.oss.ee.fault.tolerance.impl;

import java.lang.reflect.Method;

import org.eclipse.microprofile.faulttolerance.ExecutionContext;

public class ExecutionContextImpl implements ExecutionContext {
    private final Method failedCallee;
    private final Object[] params;
    private final String identifier;

    public ExecutionContextImpl(Method failed, Object[] params) {
        this.failedCallee = failed;
        this.params = params;
        this.identifier = String.format("%s#%s", failed.getDeclaringClass().getName(), failed.getName());
    }

    @Override
    public Method getMethod() {
        return failedCallee;
    }

    @Override
    public Object[] getParameters() {
        return params;
    }

    public String getIdentifier() {
        return identifier;
    }
}
