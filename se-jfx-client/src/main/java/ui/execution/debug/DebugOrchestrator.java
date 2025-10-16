package ui.execution.debug;

import engine.Engine;
import javafx.beans.property.BooleanProperty;
import javafx.stage.Window;

import java.util.function.Supplier;

public class DebugOrchestrator {
    private final Engine engine;
    private final Supplier<Window> ownerWindowSupplier;
    private final Supplier<Integer> expansionDegreeSupplier;
    private final BooleanProperty isDebugInProgress;
    private final DebugResultPresenter resultPresenter;
    private final Supplier<String> selectedOperationKeySupplier;

    private DebugCoordinator debugCoordinator;
    public DebugOrchestrator(Engine engine,
                             Supplier<Window> ownerWindowSupplier,
                             Supplier<Integer> expansionDegreeSupplier,
                             BooleanProperty isDebugInProgress,
                             DebugResultPresenter resultPresenter,
                             Supplier<String> selectedOperationKeySupplier) {
        this.engine = engine;
        this.ownerWindowSupplier = ownerWindowSupplier;
        this.expansionDegreeSupplier = expansionDegreeSupplier;
        this.isDebugInProgress = isDebugInProgress;
        this.resultPresenter = resultPresenter;
        this.selectedOperationKeySupplier = selectedOperationKeySupplier;
    }

    private void ensureCoordinator() {
        if (debugCoordinator == null) {
            debugCoordinator = new DebugCoordinator(
                    engine,
                    ownerWindowSupplier.get(),
                    expansionDegreeSupplier::get,
                    selectedOperationKeySupplier,
                    resultPresenter
            );
        }
    }

    public void debug() {
        if (isDebugInProgress.get()) return;
        ensureCoordinator();
        debugCoordinator.executeForDebug();
    }
}
