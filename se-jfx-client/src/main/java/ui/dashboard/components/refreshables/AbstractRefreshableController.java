package ui.dashboard.components.refreshables;

import dto.dashboard.UserDTO;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import util.support.AutoRefresher;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractRefreshableController  {

    private AutoRefresher autoRefresher;
    private final AtomicInteger consecutiveFails = new AtomicInteger(0);

    protected int maxFailsBeforeStop() { return 3; }
    protected abstract String logTag();
    protected abstract void fetchOnce();

    public void startAutoRefresh(int intervalMilliSeconds) {
        if (autoRefresher == null) {
            autoRefresher = new AutoRefresher(this::fetchOnce, intervalMilliSeconds);
        }
        autoRefresher.start();
    }

    public void stopAutoRefresh() {
        if (autoRefresher != null) autoRefresher.stop();
    }

    protected void onFX(Runnable runnable) {
        if (runnable != null) {
            Platform.runLater(runnable);
        }
    }

    protected <T> void replaceIfChanged(ObservableList<T> targetList, List<T> incomingList) {
        if (!isEqualLists(targetList, incomingList)) {
            targetList.setAll(incomingList);
        }
    }

    protected static <T> boolean isEqualLists(List<T> list1, List<T> list2) {
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            if (!Objects.equals(list1.get(i), list2.get(i))) return false;
        }
        return true;
    }

    protected void handleNetworkFailure(String message) {
        int n = consecutiveFails.incrementAndGet();
        System.err.println("[" + logTag() + "] " + message + " (fail #" + n + ")");
        if (n >= maxFailsBeforeStop()) {
            System.err.println("[" + logTag() + "] Reached " + maxFailsBeforeStop() + " consecutive failures. Stopping.");
            onFX(this::stopAutoRefresh);
        }
    }

    protected void handleUnauthorizedStop() {
        onFX(this::stopAutoRefresh);
    }

    protected void resetFailures() {
        consecutiveFails.set(0);
    }
}
