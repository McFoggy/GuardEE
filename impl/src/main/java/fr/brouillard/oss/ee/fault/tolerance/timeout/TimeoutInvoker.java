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
package fr.brouillard.oss.ee.fault.tolerance.timeout;

import java.util.UUID;

import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

import fr.brouillard.oss.ee.fault.tolerance.EEGuardException;
import fr.brouillard.oss.ee.fault.tolerance.config.Configurator;
import fr.brouillard.oss.ee.fault.tolerance.config.TimeoutContext;
import fr.brouillard.oss.ee.fault.tolerance.impl.Invoker;
import fr.brouillard.oss.ee.fault.tolerance.impl.InvokerChain;
import fr.brouillard.oss.ee.fault.tolerance.impl.TimeoutManager;
import fr.brouillard.oss.ee.fault.tolerance.misc.CallContext;

public class TimeoutInvoker implements Invoker {
    @Inject
    private TimeoutManager tm;
    
    @Inject
    Configurator cfg;
    
    @Override
    public Object invoke(InvocationContext context, InvokerChain chain) throws Exception {
        TimeoutContext tctx = cfg.timeout(context).orElseThrow(() -> new EEGuardException());
        String uuid = UUID.randomUUID().toString();

        Thread executingThread = Thread.currentThread();
        CallContext callContext = tm.register(uuid, tctx.toMillis(), executingThread);
        
        try {
            Object result = chain.invoke(context);
            if (callContext.wasTimeoutReached()) {
                throw new TimeoutException();
            }
            return result;
        } catch (Throwable t) {
            if (t instanceof TimeoutException) {
                throw t;
            }
            if (t instanceof InterruptedException && callContext.wasTimeoutReached()) {
                throw new TimeoutException();
            }
            throw t;
        } finally {
            tm.cancelTimerByUUID(uuid);
        }
    }
}
