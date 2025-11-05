package ui.run;

import dto.execution.ProgramDTO;
import engine.Engine;
import javafx.beans.property.BooleanProperty;

import java.util.List;
import java.util.function.Supplier;
import javafx.stage.Window;

public class RunOrchestrator {
    private final Engine engine;
    private final Supplier<Window> ownerWindowSupplier;
    private final Supplier<Integer> expansionDegreeSupplier;
    private final BooleanProperty isRunInProgress;
    private final RunResultPresenter resultPresenter;
    private final Supplier<String> selectedOperationKeySupplier;

    private RunCoordinator runCoordinator;
    public RunOrchestrator(Engine engine,
                           Supplier<Window> ownerWindowSupplier,
                           Supplier<Integer> expansionDegreeSupplier,
                           BooleanProperty isRunInProgress,
                           RunResultPresenter resultPresenter,
                           Supplier<String> selectedOperationKeySupplier) {
        this.engine = engine;
        this.ownerWindowSupplier = ownerWindowSupplier;
        this.expansionDegreeSupplier = expansionDegreeSupplier;
        this.isRunInProgress = isRunInProgress;
        this.resultPresenter = resultPresenter;
        this.selectedOperationKeySupplier = selectedOperationKeySupplier;
    }

    private void ensureCoordinator() {
        if (runCoordinator == null) {
            runCoordinator = new RunCoordinator(
                    engine,
                    ownerWindowSupplier.get(),
                    expansionDegreeSupplier::get,
                    selectedOperationKeySupplier::get,
                    resultPresenter
            );
        }
    }

    public void run(ProgramDTO program) {
        if (program == null || isRunInProgress.get()) return;
        ensureCoordinator();
        runCoordinator.executeForRun(program);
    }

    public void seedPrefillInputs(String selectedOperationKey, List<Long> inputs) {
        ensureCoordinator();
        runCoordinator.seedPrefillInputs(selectedOperationKey, inputs);
    }
}
