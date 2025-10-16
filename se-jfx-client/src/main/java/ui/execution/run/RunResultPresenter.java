package ui.execution.run;

import dto.ProgramExecutorDTO;

public interface RunResultPresenter {
    void onRunStarted();
    void onRunSucceeded(ProgramExecutorDTO result);
    void onRunFailed(String message);
}
