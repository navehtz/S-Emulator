package ui.support;

import dto.ProgramExecutorDTO;
import javafx.scene.control.Label;
import ui.components.VariablesTableController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class VariablesPaneUpdater {
    private final VariablesTableController varsPaneController;
    private final Label cyclesLabel;

    public VariablesPaneUpdater(VariablesTableController varsPaneController, Label cyclesLabel) {
        this.varsPaneController = varsPaneController;
        this.cyclesLabel = cyclesLabel;
    }

    public void update(ProgramExecutorDTO executionResult) {
        Map<String, Long> sortedVariables = new LinkedHashMap<>();
        sortedVariables.put("y", executionResult.result());
        sortedVariables.putAll((Map<? extends String, ? extends Long>) executionResult.variablesToValuesSorted().entrySet().stream()
                // Filter out variables that start with "x" or "X"
                .filter(entry -> !entry.getKey().toLowerCase().startsWith("x"))
                // Collect the remaining entries into a sorted LinkedHashMap
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                )));

        varsPaneController.setVariables(sortedVariables);
        cyclesLabel.setText(String.valueOf(executionResult.totalCycles()));
    }
}
