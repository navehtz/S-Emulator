package util.support;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AutoRefresher {

    private ScheduledExecutorService scheduler;
    private final Runnable task;
    private final int intervalSeconds;

    public AutoRefresher(Runnable task, int intervalMilliSeconds) {
        this.task = task;
        this.intervalSeconds = intervalMilliSeconds;
    }

    public void start() {
        if (scheduler != null && !scheduler.isShutdown()) return;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(task, 0, intervalSeconds, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }
}
