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
package fr.brouillard.oss.ee.fault.tolerance.impl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class TimeoutManager {
    @Resource
    TimerService timerService;

    private ConcurrentMap<String, TimeoutHandler> timeoutThreads;

    @PostConstruct
    public void initialize() {
        timeoutThreads = new ConcurrentHashMap<>();
    }
    
    @PreDestroy
    public void cleanup() {
        timerService.getAllTimers().stream().forEach(t -> {
            cancelTimer(t);
        });
    }

    public void register(String uuid, long timeoutDelay, Thread executingThread) {
        timeoutThreads.computeIfAbsent(uuid, ignore -> {
            timerService.createTimer(timeoutDelay, uuid);
            return new TimeoutHandler(executingThread);
        });
    }

    @Timeout
    public void timeout(Timer timer) {
        Serializable timerInfo = timer.getInfo();
        if (timerInfo instanceof String) {
            String uuid = (String) timerInfo;
            TimeoutHandler th = timeoutThreads.get(uuid);
            th.timeout();
        }
    }

    public void cancelTimerByUUID(String uuid) {
        timerService.getTimers().stream().filter(t -> uuid.equals(t.getInfo())).findFirst().ifPresent(this::cancelTimer);
        timeoutThreads.remove(uuid);
    }

    private void cancelTimer(Timer t) {
        try {
            t.cancel();
        } catch (java.lang.IllegalStateException | javax.ejb.EJBException timerException) {
            // TODO add traces
        }
    }

    public boolean hasReachedTimeout(String uuid) {
        TimeoutHandler th = timeoutThreads.get(uuid);
        return (th!=null)?th.hasReachedTimeout():false;
    }

    private class TimeoutHandler {
        private AtomicBoolean timeoutReached = new AtomicBoolean(false);
        private WeakReference<Thread> executingThread;

        public TimeoutHandler(Thread t) {
            executingThread = new WeakReference<Thread>(t);
        }

        public void timeout() {
            timeoutReached.set(true);
            Thread t = executingThread.get();
            if (t != null) {
                synchronized (t) {
                    t.interrupt();
                }
            }
        }

        public boolean hasReachedTimeout() {
            return timeoutReached.get();
        }
    }




}
