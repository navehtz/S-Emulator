package ui.run;

import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import engine.Engine;
import javafx.concurrent.Task;
import javafx.stage.Window;

import java.util.*;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class RunCoordinator {
    private final Engine engine;
    private final Window ownerWindow;
    private final IntSupplier expansionDegreeSupplier;
    private final Supplier<String> selectedOperationKeySupplier;
    private final RunResultPresenter resultPresenter;

    private final Map<String, Map<String, Double>> lastInputsByProgram = new HashMap<>();

    public RunCoordinator(  Engine engine,
                            Window ownerWindow,
                            IntSupplier expansionDegreeSupplier,
                            Supplier<String> selectedOperationKeySupplier,
                            RunResultPresenter resultPresenter ) {

        this.engine = Objects.requireNonNull(engine, "engine");
        this.ownerWindow = ownerWindow;
        this.expansionDegreeSupplier = Objects.requireNonNull(expansionDegreeSupplier, "expansionDegreeSupplier");
        this.selectedOperationKeySupplier = Objects.requireNonNull(selectedOperationKeySupplier, "selectedOperationKeySupplier");
        this.resultPresenter = Objects.requireNonNull(resultPresenter, "presenter");
    }

    public void executeForRun(ProgramDTO program) {
        final String selectedOperationKey = selectedOperationKeySupplier.get();
        if (selectedOperationKey == null || selectedOperationKey.isBlank()) return;

        final List<String> requiredInputs = engine
                .getExpandedProgramDTO(selectedOperationKey, expansionDegreeSupplier.getAsInt()).inputVariables();
        Map<String, Double> prefill = lastInputsByProgram.getOrDefault(selectedOperationKey, Collections.emptyMap());
        RunInputsDialog dialog = new RunInputsDialog(ownerWindow, requiredInputs, prefill);
        Optional<Map<String, Double>> userValues = dialog.showAndWait();
        if (userValues.isEmpty()) return; // user canceled

        Map<String, Double> provided = userValues.get();
        // remember for next time
        lastInputsByProgram.put(selectedOperationKey, new LinkedHashMap<>(provided));

        resultPresenter.onRunStarted();

        final int degree = expansionDegreeSupplier.getAsInt();

        Task<ProgramExecutorDTO> task = new Task<>() {
            @Override
            protected ProgramExecutorDTO call() throws Exception {
                Long[] argv = requiredInputs.stream()
                                .map(name -> provided.getOrDefault(name, 0.0))
                                .map(Double::longValue)
                                .toArray(Long[]::new);
                engine.runProgram(selectedOperationKey, degree, argv);

                return engine.getProgramToDisplayAfterRun();
            }
        };

        task.setOnSucceeded(ev -> resultPresenter.onRunSucceeded(task.getValue()));
        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            resultPresenter.onRunFailed(ex != null ? ex.getMessage() : "Unknown error");
        });

        new Thread(task, "run-exec").start();
    }
}
