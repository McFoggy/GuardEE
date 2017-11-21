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
package fr.brouillard.oss.ee.fault.tolerance.bulkhead;

import java.util.Optional;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.brouillard.oss.ee.fault.tolerance.config.BulkheadContext;
import fr.brouillard.oss.ee.fault.tolerance.config.Configurator;
import fr.brouillard.oss.ee.fault.tolerance.impl.Invoker;
import fr.brouillard.oss.ee.fault.tolerance.impl.InvokerChain;
import fr.brouillard.oss.ee.fault.tolerance.misc.DelegateFuture;

@ApplicationScoped
public class BulkheadInvoker implements Invoker {
    private final static Logger LOGGER = LoggerFactory.getLogger(BulkheadInvoker.class);
    
    @Inject
    Configurator config;
    
    @Resource
    ManagedExecutorService mes;
    
    @Override
    public Object invoke(InvocationContext context, InvokerChain chain) throws Exception {
        Optional<BulkheadContext> optBulkhead = config.bulkhead(context);
        
        if (!optBulkhead.isPresent()) {
            // No bulkhead found
            LOGGER.debug("no bulkhead configuration found");
            return chain.invoke(context);
        }
        
        BulkheadContext bulkhead = optBulkhead.get();
        if (!bulkhead.isAsynchronous()) {
            return callSynchronously(bulkhead, context, chain);
        } else {
            return callAsynchronously(bulkhead, context, chain);
        }
    }

    private Object callAsynchronously(BulkheadContext bulkhead, InvocationContext context, InvokerChain chain) {
        if (!bulkhead.acquireWaiting()) {
            throw new BulkheadException("cannot acquire a slot in bulkhead waiting queue");
        }
        
        return new DelegateFuture(mes.submit(() -> {
            try {
                return callSynchronouslyWithWait(bulkhead, context, chain);
            } finally {
                bulkhead.releaseWaiting();
            }
        }));
    }

    private Object callSynchronously(BulkheadContext bulkhead, InvocationContext context, InvokerChain chain) throws Exception {
        if (bulkhead.acquireExecution()) {
            try {
                return chain.invoke(context);
            } finally {
                bulkhead.releaseExecution();
            }
        } else {
            throw new BulkheadException("could not acquire execution slot for synchronous invocation");
        }
    }
    
    private Object callSynchronouslyWithWait(BulkheadContext bulkhead, InvocationContext context, InvokerChain chain) throws Exception {
        if (bulkhead.acquireExecutionWithWait()) {
            try {
                return chain.invoke(context);
            } finally {
                bulkhead.releaseExecution();
            }
        } else {
            throw new BulkheadException("could not acquire execution slot for synchronous invocation");
        }
    }
}
