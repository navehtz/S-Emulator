package ui.execution.support;

import dto.ProgramExecutorDTO;
import javafx.scene.control.Label;
import ui.execution.components.variableTable.VariablesTableController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
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

    public void update(ProgramExecutorDTO exec, Set<String> changedNames) {
        varsPaneController.setVariables(exec.variablesToValuesSorted());
        varsPaneController.highlightChanged(changedNames);
        cyclesLabel.setText(String.valueOf(exec.totalCycles())); //TODO: Check
    }
}
