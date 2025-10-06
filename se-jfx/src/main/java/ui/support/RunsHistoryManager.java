package ui.support;

import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import ui.components.RunHistoryTableController;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RunsHistoryManager {
    private final RunHistoryTableController runsPaneController;
    //private ObjectProperty<ProgramDTO> currentSelectedProgramProperty;
    private Supplier<String> currentProgramKeySupplier;
    private final Map<String, List<RunHistoryTableController.RunRow>> perProgramRows = new HashMap<>();
    private final Map<String, Integer> perProgramCounter = new HashMap<>();
    private int runNum = 0;

    public RunsHistoryManager(RunHistoryTableController runsPaneController) {
        this.runsPaneController = runsPaneController;
    }

    public void setCurrentProgramKeySupplier(Supplier<String> supplier) {
        this.currentProgramKeySupplier = supplier;
    }

    public void onContextChanged(String programKey) {
        List<RunHistoryTableController.RunRow> rows =
                perProgramRows.getOrDefault(programKey, Collections.emptyList());
        Platform.runLater(() -> runsPaneController.replaceAll(rows));
    }

    public void append(ProgramExecutorDTO executionResult) {
        String programKey = executionResult.programDTO().programName();
        append(programKey, executionResult.degree(), executionResult.result(), executionResult.totalCycles());

////        String inputs = executionResult.inputsValuesOfUser().stream()
////                        .map(String::valueOf)
////                        .collect(Collectors.joining(", "));
//
//        RunHistoryTableController.RunRow row = new RunHistoryTableController.RunRow(
//                ++runNum,
//                executionResult.degree(),
//                //inputs,
//                executionResult.result(),
//                executionResult.totalCycles()
//        );
//
//        javafx.application.Platform.runLater(() -> runsPaneController.appendRow(row));
//        //programNameToHistoryMap.
    }

    public void append(String programKey, int degree, long result, int cycles) {
        int nextIdx = perProgramCounter.merge(programKey, 1, Integer::sum);
        RunHistoryTableController.RunRow row =
                new RunHistoryTableController.RunRow(nextIdx, degree, result, cycles);

        perProgramRows.computeIfAbsent(programKey, k -> new ArrayList<>()).add(row);

        if (currentProgramKeySupplier != null &&
                programKey.equals(currentProgramKeySupplier.get())) {
            Platform.runLater(() -> runsPaneController.appendRow(row));
        }
    }

    public void clearHistory() {
        perProgramRows.clear();
        perProgramCounter.clear();
        Platform.runLater(runsPaneController::clearHistory);
    }
}
