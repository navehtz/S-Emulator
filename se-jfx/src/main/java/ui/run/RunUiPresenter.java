package ui.run;

import dto.execution.ProgramExecutorDTO;
import javafx.beans.property.BooleanProperty;
import ui.support.Dialogs;
import ui.support.RunsHistoryManager;
import ui.support.VariablesPaneUpdater;

import java.util.function.Consumer;

public class RunUiPresenter implements RunResultPresenter {
    private final BooleanProperty isRunInProgress;
    private final VariablesPaneUpdater variablesPaneUpdater;
    private final RunsHistoryManager runsHistoryManager;
    private final Consumer<ProgramExecutorDTO> inputsUpdater;

    public RunUiPresenter(BooleanProperty isRunInProgress,
                          VariablesPaneUpdater variablesPaneUpdater,
                          RunsHistoryManager runsHistoryManager,
                          Consumer<ProgramExecutorDTO> inputsUpdater) {
        this.isRunInProgress = isRunInProgress;
        this.variablesPaneUpdater = variablesPaneUpdater;
        this.runsHistoryManager = runsHistoryManager;
        this.inputsUpdater = inputsUpdater;
    }

    @Override
    public void onRunStarted() {
        isRunInProgress.set(true);
    }

    @Override
    public void onRunSucceeded(ProgramExecutorDTO executionResult) {
        try {
            if (inputsUpdater != null) inputsUpdater.accept(executionResult);
            variablesPaneUpdater.update(executionResult);
            runsHistoryManager.append(executionResult);
        } finally {
            isRunInProgress.set(false);
        }
    }

    @Override
    public void onRunFailed(String message) {
        isRunInProgress.set(false);
        Dialogs.error("Run failed", message, null);
    }
}
