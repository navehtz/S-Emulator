package ui.debug;

import dto.DebugDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import engine.Engine;
import javafx.concurrent.Task;
import javafx.stage.Window;
import ui.run.RunInputsDialog;
import ui.run.RunResultPresenter;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class DebugCoordinator {
    private final Engine engine;
    private final Window ownerWindow;
    private final IntSupplier expansionDegreeSupplier;
    private final Supplier<String> selectedOperationKeySupplier;
    private final DebugResultPresenter resultPresenter;

    private final Map<String, Map<String, Double>> lastInputsByProgram = new HashMap<>();

    public DebugCoordinator(Engine engine,
                            Window ownerWindow,
                            IntSupplier expansionDegreeSupplier,
                            Supplier<String> selectedOperationKeySupplier,
                            DebugResultPresenter resultPresenter ) {

        this.engine = Objects.requireNonNull(engine, "engine");
        this.ownerWindow = ownerWindow;
        this.expansionDegreeSupplier = Objects.requireNonNull(expansionDegreeSupplier, "expansionDegreeSupplier");
        this.selectedOperationKeySupplier = Objects.requireNonNull(selectedOperationKeySupplier, "selectedOperationKeySupplier");
        this.resultPresenter = Objects.requireNonNull(resultPresenter, "presenter");
    }

    public void executeForDebug() {
        final String selectedOperationKey = selectedOperationKeySupplier.get();
        if (selectedOperationKey == null || selectedOperationKey.isBlank()) return;

        final int degree = expansionDegreeSupplier.getAsInt();

        final ProgramDTO snapshot = engine.getExpandedProgramDTO(selectedOperationKey, degree);
        final List<String> requiredInputs = snapshot.inputVariables();

        Map<String, Double> prefill = lastInputsByProgram.getOrDefault(selectedOperationKey, Collections.emptyMap());
        DebugInputsDialog dialog = new DebugInputsDialog(ownerWindow, requiredInputs, prefill);
        Optional<Map<String, Double>> userValues = dialog.showAndWait();
        if (userValues.isEmpty()) return; // user canceled

        final Map<String, Double> provided = userValues.get();
        // remember for next time
        lastInputsByProgram.put(selectedOperationKey, new LinkedHashMap<>(provided));

        final List<Long> argv = requiredInputs.stream()
                        .map(name -> provided.getOrDefault(name, 0.0))
                        .map(Double::longValue)
                        .toList();

        resultPresenter.onDebugStarted();


        Task<DebugDTO> task = new Task<>() {
            @Override
            protected DebugDTO call() throws Exception {
                engine.initializeDebugger(selectedOperationKey, degree, argv);
                return engine.getProgramAfterStepOver();
            }
        };

        task.setOnSucceeded(ev -> resultPresenter.onDebugSucceeded(task.getValue()));
        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            resultPresenter.onDebugFailed(ex != null ? ex.getMessage() : "Unknown error");
        });

        new Thread(task, "debug-exec").start();
    }
}
