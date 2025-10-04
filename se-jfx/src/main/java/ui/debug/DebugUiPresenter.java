package ui.debug;

import dto.DebugDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import javafx.beans.property.BooleanProperty;
import ui.support.Dialogs;
import ui.support.RunsHistoryManager;
import ui.support.VariablesPaneUpdater;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DebugUiPresenter implements DebugResultPresenter {
    private final BooleanProperty isDebugInProgress;
    private final VariablesPaneUpdater variablesPaneUpdater;
    private final RunsHistoryManager runsHistoryManager;
    private final Consumer<ProgramExecutorDTO> inputsUpdater;
    private final Supplier<ProgramDTO> currentProgramSupplier;

    public DebugUiPresenter(BooleanProperty isDebugInProgress,
                            VariablesPaneUpdater variablesPaneUpdater,
                            RunsHistoryManager runsHistoryManager,
                            Consumer<ProgramExecutorDTO> inputsUpdater,
                            Supplier<ProgramDTO> currentProgramSupplier) {
        this.isDebugInProgress = isDebugInProgress;
        this.variablesPaneUpdater = variablesPaneUpdater;
        this.runsHistoryManager = runsHistoryManager;
        this.inputsUpdater = inputsUpdater;
        this.currentProgramSupplier = Objects.requireNonNull(currentProgramSupplier);
    }

    @Override
    public void onDebugStarted() {
        isDebugInProgress.set(true);
    }

    @Override
    public void onDebugSucceeded(DebugDTO state) {
        try {
            ProgramDTO program = currentProgramSupplier.get();
            ProgramExecutorDTO exec = toExecDTO(program, state);

            if (inputsUpdater != null) inputsUpdater.accept(exec);
            variablesPaneUpdater.update(exec);

            if (!state.hasMoreInstructions()) {
                runsHistoryManager.append(exec);
            }

        } finally {
            isDebugInProgress.set(false);
        }
    }

    @Override
    public void onDebugFailed(String message) {
        isDebugInProgress.set(false);
        Dialogs.error("Debug failed", message, null);
    }

    private static ProgramExecutorDTO toExecDTO(ProgramDTO program, DebugDTO dbg) {
        return new ProgramExecutorDTO(
                program,
                dbg.variablesToValuesSorted(),
                dbg.result(),
                dbg.totalCycles(),
                dbg.degree(),
                List.of() // inputs not needed for pane updates
        );
    }
}
