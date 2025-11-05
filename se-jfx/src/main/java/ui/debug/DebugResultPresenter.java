package ui.debug;

import dto.execution.DebugDTO;

public interface DebugResultPresenter {
    void onDebugStarted();
    void onDebugSucceeded(DebugDTO snapshot);
    void onDebugFailed(String message);
}
