package ui.run;

import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import engine.Engine;
import javafx.concurrent.Task;
import javafx.stage.Window;

import java.util.*;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;

public final class RunCoordinator {
    private final Engine engine;
    private final Window ownerWindow;
    private final IntSupplier expansionDegreeSupplier;
    private final RunResultPresenter resultPresenter;

    private final Map<String, Map<String, Double>> lastInputsByProgram = new HashMap<>();

    public RunCoordinator(  Engine engine,
                            Window ownerWindow,
                            IntSupplier expansionDegreeSupplier,
                            RunResultPresenter resultPresenter ) {

        this.engine = Objects.requireNonNull(engine, "engine");
        this.ownerWindow = ownerWindow;
        this.expansionDegreeSupplier = Objects.requireNonNull(expansionDegreeSupplier, "expansionDegreeSupplier");
        this.resultPresenter = Objects.requireNonNull(resultPresenter, "presenter");
    }

    public void executeForRun(ProgramDTO program) {
        if (program == null) return;

        List<String> requiredInputs = engine.getProgramToDisplay().inputVariables();
        Map<String, Double> prefill = lastInputsByProgram.getOrDefault(program.programName(), Collections.emptyMap());
        RunInputsDialog dialog = new RunInputsDialog(ownerWindow, requiredInputs, prefill);
        Optional<Map<String, Double>> userValues = dialog.showAndWait();
        if (userValues.isEmpty()) return; // user canceled

        Map<String, Double> provided = userValues.get();
        // remember for next time
        lastInputsByProgram.put(program.programName(), new LinkedHashMap<>(provided));

        /*Map<String, Double> inputsForRun = new LinkedHashMap<>();
        for (String xi : requiredInputs) {
            inputsForRun.put(xi, provided.getOrDefault(xi, 0.0));
        }*/

        resultPresenter.onRunStarted();

        int degree = expansionDegreeSupplier.getAsInt();

        Task<ProgramExecutorDTO> task = new Task<>() {
            @Override
            protected ProgramExecutorDTO call() throws Exception {
                engine.runProgram(degree, provided.values().stream()
                        .map(Double::longValue)
                        .toArray(Long[]::new));

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
