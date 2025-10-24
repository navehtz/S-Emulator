package ui.execution.run;

import dto.execution.ProgramExecutorDTO;

public interface RunResultPresenter {
    void onRunStarted();
    void onRunSucceeded(ProgramExecutorDTO result);
    void onRunFailed(String message);
}
