package ui.execution.debug;

import dto.execution.*;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import util.support.Dialogs;
import ui.execution.support.VariablesPaneUpdater;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class DebugUiPresenter implements DebugResultPresenter {

    private final BooleanProperty isDebugInProgress;
    private final VariablesPaneUpdater variablesPaneUpdater;
    private final Consumer<DebugDTO> applySnapshot;
    private final Runnable onOutOfCredits;
    private final LongConsumer refreshCredits;
    private final Consumer<ProgramExecutorDTO> onUpdateInputsPane;

    private final Map<String, Long> lastVarsSnapshot = new LinkedHashMap<>();

    public DebugUiPresenter(BooleanProperty isDebugInProgress,
                            VariablesPaneUpdater variablesPaneUpdater,
                            Consumer<DebugDTO> applySnapshot,
                            Runnable onOutOfCredits,
                            LongConsumer refreshCredits,
                            Consumer<ProgramExecutorDTO> onUpdateInputsPane) {
        this.isDebugInProgress = isDebugInProgress;
        this.variablesPaneUpdater = variablesPaneUpdater;
        this.applySnapshot = applySnapshot;
        this.onOutOfCredits = onOutOfCredits;
        this.refreshCredits = refreshCredits;
        this.onUpdateInputsPane = onUpdateInputsPane;
    }

    @Override
    public void onDebugStarted() {
        Platform.runLater(() -> isDebugInProgress.set(true));
    }

    @Override
    public void onDebugSnapshot(DebugResponseDTO response) {
        Platform.runLater(() -> {
            DebugDTO snap = response.snapshot();

            // Compute which variables changed
            Set<String> changedVariables = new HashSet<>();
            Map<String, Long> variablesNow = snap.variablesToValuesSorted();
            for (Map.Entry<String, Long> e : variablesNow.entrySet()) {
                Long before = lastVarsSnapshot.get(e.getKey());
                if (before == null || !before.equals(e.getValue())) {
                    changedVariables.add(e.getKey());
                }
            }
            lastVarsSnapshot.clear();
            lastVarsSnapshot.putAll(variablesNow);

            // Update variables and inputs panes
            ProgramExecutorDTO exec = toExecDTO(snap);
            variablesPaneUpdater.update(exec, changedVariables);
            if (onUpdateInputsPane != null) onUpdateInputsPane.accept(exec);

            // Refresh credits display
            if (refreshCredits != null) {
                refreshCredits.accept(response.creditsRemaining());
            }

            // Apply snapshot (highlights instruction table)
            if (applySnapshot != null) {
                applySnapshot.accept(snap);
            }

            // If program ended, signal end
            if (!snap.hasMoreInstructions()) {
                onDebugEnded();
            }
        });
    }

    @Override
    public void onDebugFailed(String message) {
        Platform.runLater(() -> {
            isDebugInProgress.set(false);
            Dialogs.error("Debug failed", message, null);
        });
    }

    @Override
    public void onDebugOutOfCredits(String message) {
        Platform.runLater(() -> {
            isDebugInProgress.set(false);
            Dialogs.error("Out of Credits", message, null);
            if (onOutOfCredits != null) onOutOfCredits.run();
        });
    }

    @Override
    public void onDebugEnded() {
        Platform.runLater(() -> {
            isDebugInProgress.set(false);
            Dialogs.info("Debug Complete", "Program execution finished.", null);
        });
    }

    private static ProgramExecutorDTO toExecDTO(DebugDTO dbg) {
        ProgramDTO stub = new ProgramDTO(
                dbg.programName(),
                List.of(), List.of(),
                new InstructionsDTO(List.of()),
                List.of(),
                List.of()
        );
        return new ProgramExecutorDTO(
                stub,
                dbg.variablesToValuesSorted(),
                dbg.result(),
                dbg.totalCycles(),
                dbg.degree(),
                dbg.inputsValuesOfUser() != null ? dbg.inputsValuesOfUser() : List.of(),
                "",
                false
        );
    }
}
