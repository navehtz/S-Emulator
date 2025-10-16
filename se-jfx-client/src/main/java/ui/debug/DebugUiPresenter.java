package ui.debug;

import dto.DebugDTO;
import dto.InstructionsDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import javafx.beans.property.BooleanProperty;
import ui.debug.DebugResultPresenter;
import ui.support.Dialogs;
import ui.support.RunsHistoryManager;
import ui.support.VariablesPaneUpdater;

import java.util.List;
import java.util.function.Consumer;

public class DebugUiPresenter implements DebugResultPresenter {
    private final BooleanProperty isDebugInProgress;
    private final VariablesPaneUpdater variablesPaneUpdater;
    private final RunsHistoryManager runsHistoryManager;
    private final Consumer<ProgramExecutorDTO> inputsUpdater;
    private final Consumer<DebugDTO> applySnapshot;
    //private final Supplier<ProgramDTO> currentProgramSupplier;
    private final Runnable onSessionStarted;

    public DebugUiPresenter(BooleanProperty isDebugInProgress,
                            VariablesPaneUpdater variablesPaneUpdater,
                            RunsHistoryManager runsHistoryManager,
                            Consumer<ProgramExecutorDTO> inputsUpdater,
                            Consumer<DebugDTO> applySnapshot,
                            Runnable onSessionStarted) {
        this.isDebugInProgress = isDebugInProgress;
        this.variablesPaneUpdater = variablesPaneUpdater;
        this.runsHistoryManager = runsHistoryManager;
        this.inputsUpdater = inputsUpdater;
        this.applySnapshot = applySnapshot;
        this.onSessionStarted = onSessionStarted;
    }

    @Override
    public void onDebugStarted() {
        isDebugInProgress.set(true);
        if (onSessionStarted != null) onSessionStarted.run();
    }

    @Override
    public void onDebugSucceeded(DebugDTO state) {
        //ProgramDTO program = currentProgramSupplier.get();

        ProgramExecutorDTO exec = toExecDTO(state);

        if (inputsUpdater != null) inputsUpdater.accept(exec);
        variablesPaneUpdater.update(exec);

        if (!state.hasMoreInstructions()) {
            runsHistoryManager.append(exec);
        }
        if (applySnapshot != null) applySnapshot.accept(state);
    }

    @Override
    public void onDebugFailed(String message) {
        isDebugInProgress.set(false);
        Dialogs.error("Debug failed", message, null);
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
                List.of() // inputs not needed for pane updates
        );
    }
}
