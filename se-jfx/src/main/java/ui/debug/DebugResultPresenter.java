package ui.debug;

import dto.DebugDTO;
import dto.ProgramExecutorDTO;

public interface DebugResultPresenter {
    void onDebugStarted();
    void onDebugSucceeded(DebugDTO result);
    void onDebugFailed(String message);
}
