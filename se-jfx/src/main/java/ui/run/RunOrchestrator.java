package ui.run;

import dto.ProgramDTO;
import engine.Engine;
import javafx.beans.property.BooleanProperty;

import java.util.function.Supplier;
import javafx.stage.Window;

public class RunOrchestrator {
    private final Engine engine;
    private final Supplier<Window> ownerWindowSupplier;
    private final Supplier<Integer> expansionDegreeSupplier;
    private final BooleanProperty isRunInProgress;
    private final RunResultPresenter resultPresenter;

    private RunCoordinator runCoordinator;
    public RunOrchestrator(Engine engine,
                           Supplier<Window> ownerWindowSupplier,
                           Supplier<Integer> expansionDegreeSupplier,
                           BooleanProperty isRunInProgress,
                           RunResultPresenter resultPresenter) {
        this.engine = engine;
        this.ownerWindowSupplier = ownerWindowSupplier;
        this.expansionDegreeSupplier = expansionDegreeSupplier;
        this.isRunInProgress = isRunInProgress;
        this.resultPresenter = resultPresenter;
    }

    private void ensureCoordinator() {
        if (runCoordinator == null) {
            runCoordinator = new RunCoordinator(
                    engine,
                    ownerWindowSupplier.get(),
                    expansionDegreeSupplier::get,
                    resultPresenter
            );
        }
    }

    public void run(ProgramDTO program) {
        if (program == null || isRunInProgress.get()) return;
        ensureCoordinator();
        runCoordinator.executeForRun(program);
    }
}
