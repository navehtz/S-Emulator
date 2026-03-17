package ui.execution.debug;

import dto.execution.DebugResponseDTO;

public interface DebugResultPresenter {
    void onDebugStarted();
    void onDebugSnapshot(DebugResponseDTO response);
    void onDebugFailed(String message);
    void onDebugOutOfCredits(String message);
    void onDebugEnded();
}
