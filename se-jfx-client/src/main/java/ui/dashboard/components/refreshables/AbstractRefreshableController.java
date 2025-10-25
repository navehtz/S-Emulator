package ui.dashboard.components.refreshables;

import util.support.AutoRefresher;

public abstract class AbstractRefreshableController  {

    private AutoRefresher autoRefresher;

    protected abstract void fetchOnce();

    public void startAutoRefresh(int intervalMilliSeconds) {
        if (autoRefresher == null)
            autoRefresher = new AutoRefresher(this::fetchOnce, intervalMilliSeconds);
        autoRefresher.start();
    }

    public void stopAutoRefresh() {
        if (autoRefresher != null) autoRefresher.stop();
    }
}
