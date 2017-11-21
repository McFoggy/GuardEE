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
package fr.brouillard.oss.ee.fault.tolerance.config;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BulkheadContext {
    private final static Logger LOGGER = LoggerFactory.getLogger(BulkheadContext.class);

    private final Semaphore bulkheadSemaphore;
    private final Semaphore waitingSemaphore;

    private final boolean asynchronous;
    private final int size;
    private final int waitingSize;

    private final AtomicInteger passedWaiting = new AtomicInteger(0);
    private final AtomicInteger passedExecuting = new AtomicInteger(0);

    public BulkheadContext(Bulkhead bulkAnnotation, boolean asynchronuous) {
        this.asynchronous = asynchronuous;
        this.size = bulkAnnotation.value();
        this.waitingSize = bulkAnnotation.value() + bulkAnnotation.waitingTaskQueue();

        if (size <= 0) {
            throw new IllegalArgumentException("Bulkhead size must be >= 0");
        }
        if (asynchronuous && waitingSize <= size) {
            throw new IllegalArgumentException("Bulkhead waitingTaskQueue must be >= 0");
        }

        this.bulkheadSemaphore = new Semaphore(size, true);
        if (asynchronuous) {
            this.waitingSemaphore = new Semaphore(waitingSize, true);
        } else {
            this.waitingSemaphore = null;
        }
    }

    public boolean acquireWaiting() {
        if (asynchronous && waitingSemaphore != null) {
            try {
                boolean waitingAllowed = waitingSemaphore.tryAcquire(0, TimeUnit.SECONDS);      // to keep fairness
                if (waitingAllowed) {
                    passedWaiting.incrementAndGet();
                    LOGGER.debug("[WAIT][PASSED] {}::{}, {}", passedWaiting.get(), passedExecuting.get(), this);
                } else {
                    LOGGER.debug("[WAIT][FAILED] {}::{}, {}", passedWaiting.get(), passedExecuting.get(), this);
                }
                return waitingAllowed;
            } catch (InterruptedException ie) {
                return false;
            }
        }

        return true;
    }

    public void releaseWaiting() {
        if (asynchronous && waitingSemaphore != null) {
            passedWaiting.decrementAndGet();
            waitingSemaphore.release();
            LOGGER.debug("[WAIT][ENDED] {}::{}, {}", passedWaiting.get(), passedExecuting.get(), this);
        }
    }

    public boolean acquireExecution() {
        try {
            boolean executionAllowed = bulkheadSemaphore.tryAcquire(0, TimeUnit.SECONDS);;
            if (executionAllowed) {
                passedExecuting.incrementAndGet();
                LOGGER.debug("[EXECUTE][PASSED] {}::{}, {}", passedWaiting.get(), passedExecuting.get(), this);
            } else {
                LOGGER.debug("[EXECUTE][FAILED] {}::{}, {}", passedWaiting.get(), passedExecuting.get(), this);
            }
            return executionAllowed;
        } catch (InterruptedException ie) {
            LOGGER.debug("[EXECUTE][FAILED] by interruption {}::{}, {}", passedWaiting.get(), passedExecuting.get(), this);
            return false;
        }
    }
    
    public boolean acquireExecutionWithWait() {
        try {
            bulkheadSemaphore.acquire();
            LOGGER.debug("[EXECUTE][PASSED] {}::{}, {}", passedWaiting.get(), passedExecuting.get(), this);
            return true;
        } catch (InterruptedException ie) {
            LOGGER.debug("[EXECUTE][FAILED] by interruption {}::{}, {}", passedWaiting.get(), passedExecuting.get(), this);
            return false;
        }
    }

    public void releaseExecution() {
        passedExecuting.decrementAndGet();
        bulkheadSemaphore.release();
        LOGGER.debug("[EXECUTE][ENDED] {}::{}, {}", passedWaiting.get(), passedExecuting.get(), this);
    }

    public boolean isAsynchronous() {
        return asynchronous;
    }

    @Override
    public String toString() {
        return "BulkheadContext{" +
                "id=" + System.identityHashCode(this) +
                ", asynchronous=" + asynchronous +
                ", size=" + size +
                ", available=" + bulkheadSemaphore.availablePermits() +
                ((asynchronous)?", waitingSize=" + waitingSize +", waiting available=" + waitingSemaphore.availablePermits():"") +
                '}';
    }
}
