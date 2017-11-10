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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class TimeoutManager {
    private final static Logger LOGGER = LoggerFactory.getLogger(TimeoutManager.class);
    
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
            try {
                t.cancel();
            } catch (Exception ex) {
                LOGGER.debug("unexpected exception while cancelling timer", ex);
            }
        });
    }

    public void register(String uuid, long timeoutDelayInMillis, Thread executingThread) {
        LOGGER.debug("for key[{}], registering a timeout of {}ms for thread [{}]", uuid, timeoutDelayInMillis, executingThread.getName());
        
        timeoutThreads.compute(uuid, (key, oldTH) -> {
            if (oldTH != null) {
                LOGGER.warn("an existing TimerHandler was found under key::{}", uuid);
            }
            TimerConfig tc = new TimerConfig(uuid, false);
            timerService.createSingleActionTimer(timeoutDelayInMillis, tc);
            return new TimeoutHandler(executingThread);
        });
    }

    @Timeout
    public void timeout(Timer timer) {
        try {
            Serializable timerInfo = timer.getInfo();
            
            if (timerInfo instanceof String) {
                String uuid = (String) timerInfo;
                LOGGER.debug("timeout reached for key[{}]", uuid);
                TimeoutHandler th = timeoutThreads.remove(uuid);
                th.timeout();
            }
        } catch (Exception ex) {
            LOGGER.debug("unexpected exception while firing timeout", ex);
        }
    }

    public void cancelTimerByUUID(String uuid) {
        if (timeoutThreads.remove(uuid) != null) {
            try {
                timerService.getTimers().stream().filter(t -> uuid.equals(t.getInfo())).findFirst().ifPresent(Timer::cancel);
            } catch (Exception ex) {
                LOGGER.debug("unexpected exception while accessing EJB timer for key[{}]", uuid, ex);
            }
            LOGGER.debug("timer cancelled for key[{}]", uuid);
        } else {
            LOGGER.trace("timer for key[{}] was already cancelled", uuid);
        }
    }

    public boolean hasReachedTimeout(String uuid) {
        TimeoutHandler th = timeoutThreads.get(uuid);
        return (th!=null)?th.hasReachedTimeout():true;
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
                LOGGER.debug("timeout reached, interrupting thread[{}]", t.getName());
                synchronized (t) {
                    t.interrupt();
                }
            } else {
                LOGGER.trace("timeout reached but thread has gone");
            }
        }

        public boolean hasReachedTimeout() {
            return timeoutReached.get();
        }
    }
}
