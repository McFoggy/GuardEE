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
package fr.brouillard.oss.ee.fault.tolerance.bulkhead;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

public class BulkheadTest {
    @Test(timeOut = 1000)
    public void acquisitionTentativeOnSemaphoreIsQuick() {
        int maxAllowed = 10;
        int nbCalls = 100;
        
        Semaphore s = new Semaphore(maxAllowed, true);
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger notAcquired = new AtomicInteger(0);
        AtomicBoolean failure = new AtomicBoolean(false);
        
        ExecutorService es = Executors.newFixedThreadPool(50);

        for (int i = 0; i < nbCalls; i++) {
            es.submit(() -> {
                try {
                    if (s.tryAcquire(0, TimeUnit.SECONDS)) {
                        System.out.println("SUCCESS: " + System.currentTimeMillis());
                        success.incrementAndGet();
                    } else {
                        System.out.println("NOT ACQUIRED: " + System.currentTimeMillis());
                        notAcquired.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    failure.set(true);
                }
            });
        }

        Assert.assertFalse(failure.get());
    }
}
