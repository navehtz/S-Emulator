package ui.execution.run;

import dto.execution.ExecutionStatusDTO;
import dto.execution.ProgramDTO;
import dto.execution.ProgramExecutorDTO;
import dto.execution.RunState;
import javafx.concurrent.Task;
import javafx.stage.Window;

import java.util.*;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class RunCoordinator {
    private final RunGateway runGateway;
    private final Window ownerWindow;
    private final IntSupplier expansionDegreeSupplier;
    private final Supplier<String> selectedOperationKeySupplier;
    private final Supplier<String> selectedArchitectureSupplier;
    private final RunResultPresenter resultPresenter;

    private final Map<String, Map<String, Double>> lastInputsByProgram = new HashMap<>();

    public RunCoordinator(  RunGateway runGateway,
                            Window ownerWindow,
                            IntSupplier expansionDegreeSupplier,
                            Supplier<String> selectedOperationKeySupplier,
                            Supplier<String> selectedArchitectureSupplier,
                            RunResultPresenter resultPresenter ) {

        this.runGateway = Objects.requireNonNull(runGateway, "runGateway");
        this.ownerWindow = ownerWindow;
        this.expansionDegreeSupplier = Objects.requireNonNull(expansionDegreeSupplier, "expansionDegreeSupplier");
        this.selectedOperationKeySupplier = Objects.requireNonNull(selectedOperationKeySupplier, "selectedOperationKeySupplier");
        this.selectedArchitectureSupplier = Objects.requireNonNull(selectedArchitectureSupplier, "selectedArchitectureSupplier");
        this.resultPresenter = Objects.requireNonNull(resultPresenter, "presenter");
    }

    public void executeForRun(ProgramDTO program) {
        final String programName = selectedOperationKeySupplier.get();
        if (programName == null || programName.isBlank()) return;

        final int degree = expansionDegreeSupplier.getAsInt();

        final List<String> requiredInputs;
        try {
            requiredInputs = runGateway.fetchRequiredInputs(programName, degree);
        } catch (Exception ex) {
            resultPresenter.onRunFailed("Failed to get inputs: " + ex.getMessage());
            return;
        }

        Map<String, Double> prefill = lastInputsByProgram.getOrDefault(programName, Collections.emptyMap());
        RunInputsDialog dialog = new RunInputsDialog(ownerWindow, requiredInputs, prefill);
        Optional<Map<String, Double>> userValues = dialog.showAndWait();
        if (userValues.isEmpty()) return; // user canceled

        Map<String, Double> provided = userValues.get();
        // remember for next time
        lastInputsByProgram.put(programName, new LinkedHashMap<>(provided));

        resultPresenter.onRunStarted();

        final String architecture = Optional.ofNullable(selectedArchitectureSupplier.get()).orElse("I");

        Task<ProgramExecutorDTO> task = new Task<>() {
            @Override
            protected ProgramExecutorDTO call() throws Exception {
                // Submit
                List<Long> inputsList = requiredInputs.stream()
                        .map(name -> provided.getOrDefault(name, 0.0))
                        .map(Double::longValue)
                        .toList();

                String runId = runGateway.submitRun(programName, architecture, degree, inputsList);

                // Poll
                final long sleepMilliseconds = 150L;
                while (true) {
                    ExecutionStatusDTO st = runGateway.getStatus(runId);
                    if (st == null) throw new IllegalStateException("Unknown runId: " + runId);

                    if (st.state() == RunState.DONE) break;
                    if (st.state() == RunState.ERROR) throw new RuntimeException(
                            st.message() == null || st.message().isBlank() ? "Execution failed" : st.message());
                    if (st.state() == RunState.CANCELLED) throw new RuntimeException("Execution cancelled");

                    Thread.sleep(sleepMilliseconds);
                }

                // Fetch result to display
                return runGateway.fetchResult(programName);
            }
        };

        task.setOnSucceeded(ev -> resultPresenter.onRunSucceeded(task.getValue()));
        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            resultPresenter.onRunFailed(ex != null ? ex.getMessage() : "Unknown error");
        });

        new Thread(task, "run-exec").start();
//        Task<ProgramExecutorDTO> task = new Task<>() {
//            @Override
//            protected ProgramExecutorDTO call() throws Exception {
//                Long[] argv = requiredInputs.stream()
//                                .map(name -> provided.getOrDefault(name, 0.0))
//                                .map(Double::longValue)
//                                .toArray(Long[]::new);
//                engine.runProgram(operationKey, degree, argv);
//
//                return engine.getProgramToDisplayAfterRun();
//            }
//        };
//
//        task.setOnSucceeded(ev -> resultPresenter.onRunSucceeded(task.getValue()));
//        task.setOnFailed(ev -> {
//            Throwable ex = task.getException();
//            resultPresenter.onRunFailed(ex != null ? ex.getMessage() : "Unknown error");
//        });
//
//        new Thread(task, "run-exec").start();
    }

        public void seedPrefillInputs(String programName, List<Long> inputsValues) {
            List<String> inputsNames;
            try {
                inputsNames = runGateway.fetchRequiredInputs(programName, expansionDegreeSupplier.getAsInt());
            } catch (Exception e) {
                return;
            }

            Map<String, Double> prefill = new LinkedHashMap<>();
            for (int i = 0; i < inputsNames.size(); i++) {
                double value = (i < inputsValues.size()) ? inputsValues.get(i).doubleValue() : 0.0;
                prefill.put(inputsNames.get(i), value);
            }
            lastInputsByProgram.put(programName, prefill);
        }
}
