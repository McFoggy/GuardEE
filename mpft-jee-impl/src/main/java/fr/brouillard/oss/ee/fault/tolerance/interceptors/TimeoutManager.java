package fr.brouillard.oss.ee.fault.tolerance.interceptors;

import javax.annotation.PostConstruct;
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
