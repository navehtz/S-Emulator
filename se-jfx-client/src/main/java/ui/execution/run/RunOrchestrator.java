package ui.execution.run;

import dto.execution.ProgramDTO;
import engine.Engine;
import javafx.beans.property.BooleanProperty;

import java.util.List;
import java.util.function.Supplier;
import javafx.stage.Window;

public class RunOrchestrator {
    private final RunGateway runGateway;
    private final Supplier<Window> ownerWindowSupplier;
    private final Supplier<Integer> expansionDegreeSupplier;
    private final BooleanProperty isRunInProgress;
    private final RunResultPresenter resultPresenter;
    private final Supplier<String> selectedOperationKeySupplier;
    private final Supplier<String> selectedArchitectureSupplier;

    private RunCoordinator runCoordinator;
    public RunOrchestrator(RunGateway runGateway,
                           Supplier<Window> ownerWindowSupplier,
                           Supplier<Integer> expansionDegreeSupplier,
                           BooleanProperty isRunInProgress,
                           RunResultPresenter resultPresenter,
                           Supplier<String> selectedOperationKeySupplier,
                           Supplier<String> selectedArchitectureSupplier ) {
        this.ownerWindowSupplier = ownerWindowSupplier;
        this.expansionDegreeSupplier = expansionDegreeSupplier;
        this.isRunInProgress = isRunInProgress;
        this.resultPresenter = resultPresenter;
        this.selectedOperationKeySupplier = selectedOperationKeySupplier;
        this.selectedArchitectureSupplier = selectedArchitectureSupplier;

        this.runCoordinator = new RunCoordinator(
                runGateway,
                ownerWindowSupplier.get(),
                expansionDegreeSupplier::get,
                selectedOperationKeySupplier::get,
                selectedArchitectureSupplier::get,
                resultPresenter
        );
    }

//    private void ensureCoordinator() {
//        if (runCoordinator == null) {
//            runCoordinator = new RunCoordinator(
//                    engine,
//                    ownerWindowSupplier.get(),
//                    expansionDegreeSupplier::get,
//                    selectedOperationKeySupplier,
//                    resultPresenter
//            );
//        }
//    }

    public void run(ProgramDTO program) {
        if (program == null || isRunInProgress.get()) return;
        //ensureCoordinator();
        runCoordinator.executeForRun(program);
    }

    public void seedPrefillInputs(String selectedOperationKey, List<Long> inputs) {
        //ensureCoordinator();
        runCoordinator.seedPrefillInputs(selectedOperationKey, inputs);
    }
}
