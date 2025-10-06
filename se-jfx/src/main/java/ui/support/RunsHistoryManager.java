package ui.support;

import dto.ProgramExecutorDTO;
import javafx.application.Platform;
import ui.components.RunHistoryTableController;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RunsHistoryManager {
    private final RunHistoryTableController runsPaneController;
    //private ObjectProperty<ProgramDTO> currentSelectedProgramProperty;
    private Supplier<String> currentProgramKeySupplier;
    private final Map<String, List<RunHistoryTableController.RunRow>> perProgramRows = new HashMap<>();
    private final Map<String, List<ProgramExecutorDTO>> perProgramDTOs = new HashMap<>();
    private final Map<String, Integer> perProgramCounter = new HashMap<>();
    private int runNum = 0;

    public RunsHistoryManager(RunHistoryTableController runsPaneController) {
        this.runsPaneController = runsPaneController;
    }

    public void setCurrentProgramKeySupplier(Supplier<String> supplier) {
        this.currentProgramKeySupplier = supplier;
    }

    public void onContextChanged(String programKey) {
        runsPaneController.replaceAll(perProgramRows.getOrDefault(programKey, Collections.emptyList()));
    }

    public void append(ProgramExecutorDTO executionResult) {
        String programKey = executionResult.programDTO().programName();
        int index = perProgramCounter.merge(programKey, 1, Integer::sum);

        List<Long> inputs = executionResult.inputsValuesOfUser();

        var row = new RunHistoryTableController.RunRow(index, programKey, executionResult.degree(), inputs, executionResult.result(), executionResult.totalCycles());
        perProgramRows.computeIfAbsent(programKey, k -> new ArrayList<>()).add(row);
        perProgramDTOs.computeIfAbsent(programKey, k -> new ArrayList<>()).add(executionResult);

        if (currentProgramKeySupplier != null && programKey.equals(currentProgramKeySupplier.get())) {
            Platform.runLater(() -> runsPaneController.appendRow(row));
        }
        //append(programKey, executionResult.degree(), executionResult.result(), executionResult.totalCycles());

        //        String inputs = executionResult.inputsValuesOfUser().stream()
//                        .map(String::valueOf)
//                        .collect(Collectors.joining(", "));
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

//    public void append(String programKey, int degree, long result, int cycles) {
//        int nextIdx = perProgramCounter.merge(programKey, 1, Integer::sum);
//        RunHistoryTableController.RunRow row =
//                new RunHistoryTableController.RunRow(nextIdx, degree, result, cycles);
//
//        perProgramRows.computeIfAbsent(programKey, k -> new ArrayList<>()).add(row);
//
//        if (currentProgramKeySupplier != null &&
//                programKey.equals(currentProgramKeySupplier.get())) {
//            Platform.runLater(() -> runsPaneController.appendRow(row));
//        }
//    }

    public ProgramExecutorDTO dtoAt(String programKey, int rowIndex) {
        List<ProgramExecutorDTO> dtosList = perProgramDTOs.get(programKey);
        if (dtosList == null || rowIndex < 0 || rowIndex >= dtosList.size())
            return null;
        return dtosList.get(rowIndex);
    }

    public void clearHistory() {
        perProgramRows.clear();
        perProgramCounter.clear();
        perProgramDTOs.clear();
        Platform.runLater(runsPaneController::clearHistory);
    }

    public void showStatusPopup(RunHistoryTableController.RunRow row) {
        if (currentProgramKeySupplier == null) return;

        String programKey = currentProgramKeySupplier.get();
        if (programKey == null) return;

        List<ProgramExecutorDTO> dtosList = perProgramDTOs.get(programKey);
        if (dtosList == null || row.runNum() < 0 || row.runNum() > dtosList.size()) return;

        ProgramExecutorDTO exec = dtosList.get(row.runNum() - 1); // -1 since runNum is 1-based but list indices are 0-based

        if (exec == null) return;

        StringBuilder sb = new StringBuilder("Final variable values:\n\n");
        sb.append("Result = ").append(exec.result()).append("\n");
        exec.variablesToValuesSorted().forEach((name, value) ->
                sb.append(name).append(" = ").append(value).append("\n")
        );

        Dialogs.info("Program Status", sb.toString(), null);
    }
}
