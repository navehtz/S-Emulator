package ui.execution.debug;

import dto.DebugDTO;
import dto.ProgramExecutorDTO;

public interface DebugResultPresenter {
    void onDebugStarted();
    void onDebugSucceeded(DebugDTO snapshot);
    void onDebugFailed(String message);
}
