package ui.support;

import dto.ProgramExecutorDTO;
import ui.components.RunHistoryTableController;

import java.util.stream.Collectors;

public class RunsHistoryManager {
    private final RunHistoryTableController runsPaneController;
    private int runNum = 0;

    public RunsHistoryManager(RunHistoryTableController runsPaneController) {
        this.runsPaneController = runsPaneController;
    }

    public void append(ProgramExecutorDTO executionResult) {
        String inputs = executionResult.inputsValuesOfUser().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));

        RunHistoryTableController.RunRow row = new RunHistoryTableController.RunRow(
                ++runNum,
                executionResult.degree(),
                inputs,
                executionResult.result(),
                executionResult.totalCycles()
        );

        javafx.application.Platform.runLater(() -> runsPaneController.appendRow(row));
    }

    public void clearHistory() {
        runsPaneController.clearHistory();
        runNum = 0;
    }
}
