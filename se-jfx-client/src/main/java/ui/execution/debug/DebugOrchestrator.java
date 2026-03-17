package ui.execution.debug;

import dto.execution.DebugResponseDTO;
import dto.execution.ExecutionStatusDTO;
import dto.execution.ProgramDTO;
import dto.execution.RunState;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.concurrent.Task;
import javafx.stage.Window;
import util.support.Dialogs;

import java.util.*;
import java.util.function.Supplier;

public class DebugOrchestrator {

    private final DebugGateway debugGateway;
    private final Supplier<Window> ownerWindowSupplier;
    private final Supplier<Integer> expansionDegreeSupplier;
    private final BooleanProperty isDebugInProgress;
    private final DebugResultPresenter presenter;
    private final Supplier<String> selectedOperationKeySupplier;
    private final Supplier<String> selectedArchitectureSupplier;

    private volatile String sessionId;
    private final Map<String, Map<String, Double>> lastInputsByProgram = new HashMap<>();

    public DebugOrchestrator(DebugGateway debugGateway,
                             Supplier<Window> ownerWindowSupplier,
                             Supplier<Integer> expansionDegreeSupplier,
                             BooleanProperty isDebugInProgress,
                             DebugResultPresenter presenter,
                             Supplier<String> selectedOperationKeySupplier,
                             Supplier<String> selectedArchitectureSupplier) {
        this.debugGateway = debugGateway;
        this.ownerWindowSupplier = ownerWindowSupplier;
        this.expansionDegreeSupplier = expansionDegreeSupplier;
        this.isDebugInProgress = isDebugInProgress;
        this.presenter = presenter;
        this.selectedOperationKeySupplier = selectedOperationKeySupplier;
        this.selectedArchitectureSupplier = selectedArchitectureSupplier;
    }

    public void debug(ProgramDTO program) {
        if (isDebugInProgress.get()) return;

        final String programName = selectedOperationKeySupplier.get();
        if (programName == null || programName.isBlank()) return;

        final int degree = expansionDegreeSupplier.get();
        final String architecture = Optional.ofNullable(selectedArchitectureSupplier.get()).orElse("I");

        // Fetch required inputs to show dialog
        final List<String> requiredInputs;
        try {
            requiredInputs = debugGateway.fetchRequiredInputs(programName, degree);
        } catch (Exception ex) {
            presenter.onDebugFailed("Failed to fetch inputs: " + ex.getMessage());
            return;
        }

        Map<String, Double> prefill = lastInputsByProgram.getOrDefault(programName, Collections.emptyMap());
        DebugInputsDialog dialog = new DebugInputsDialog(ownerWindowSupplier.get(), requiredInputs, prefill);
        Optional<Map<String, Double>> userValues = dialog.showAndWait();
        if (userValues.isEmpty()) return;

        Map<String, Double> provided = userValues.get();
        lastInputsByProgram.put(programName, new LinkedHashMap<>(provided));

        final List<Long> inputs = requiredInputs.stream()
                .map(name -> provided.getOrDefault(name, 0.0))
                .map(Double::longValue)
                .toList();

        Task<DebugResponseDTO> task = new Task<>() {
            @Override
            protected DebugResponseDTO call() throws Exception {
                // Pre-run credits check
                double avgCycles = debugGateway.getAverageCycles(programName, degree);
                int archCost = debugGateway.getArchitectureCost(architecture);
                long currentCredits = debugGateway.getCreditsBalance();

                if (avgCycles > 0 && currentCredits < avgCycles + archCost) {
                    Platform.runLater(() ->
                            Dialogs.warning("Insufficient Credits",
                                    String.format("Estimated cost: %.0f + %d (arch) = %.0f. Current credits: %d.",
                                            avgCycles, archCost, avgCycles + archCost, currentCredits),
                                    ownerWindowSupplier.get()));
                    return null;
                }

                return debugGateway.startDebug(programName, architecture, degree, inputs);
            }
        };

        task.setOnSucceeded(ev -> {
            DebugResponseDTO response = task.getValue();
            if (response == null) return; // insufficient credits warning was shown
            sessionId = response.sessionId();
            presenter.onDebugStarted();
            presenter.onDebugSnapshot(response);
        });

        task.setOnFailed(ev -> {
            Throwable ex = task.getException();
            if (ex instanceof DebugOutOfCreditsException) {
                presenter.onDebugOutOfCredits(ex.getMessage());
            } else {
                presenter.onDebugFailed(ex != null ? ex.getMessage() : "Unknown error");
            }
        });

        new Thread(task, "debug-start").start();
    }

    public void stepOver() {
        String id = sessionId;
        if (id == null) return;

        Task<DebugResponseDTO> task = new Task<>() {
            @Override
            protected DebugResponseDTO call() throws Exception {
                return debugGateway.stepOver(id);
            }
        };

        task.setOnSucceeded(ev -> {
            DebugResponseDTO response = task.getValue();
            presenter.onDebugSnapshot(response);
            if (response != null && !response.snapshot().hasMoreInstructions()) stop();
        });
        task.setOnFailed(ev -> handleStepFailure(task.getException()));
        new Thread(task, "debug-step-over").start();
    }

    public void stepBack() {
        String id = sessionId;
        if (id == null) return;

        Task<DebugResponseDTO> task = new Task<>() {
            @Override
            protected DebugResponseDTO call() throws Exception {
                return debugGateway.stepBack(id);
            }
        };

        task.setOnSucceeded(ev -> {
            DebugResponseDTO response = task.getValue();
            presenter.onDebugSnapshot(response);
            if (response != null && !response.snapshot().hasMoreInstructions()) stop();
        });
        task.setOnFailed(ev -> handleStepFailure(task.getException()));
        new Thread(task, "debug-step-back").start();
    }

    public void resume(List<Boolean> breakpoints) {
        String id = sessionId;
        if (id == null) return;

        Task<DebugResponseDTO> task = new Task<>() {
            @Override
            protected DebugResponseDTO call() throws Exception {
                String resumeId = debugGateway.submitResume(id, breakpoints);

                final long sleepMs = 150L;
                RunState finalState = null;
                String outOfCreditsMessage = null;

                while (true) {
                    ExecutionStatusDTO status = debugGateway.getResumeStatus(resumeId);
                    if (status == null) throw new IllegalStateException("Unknown resumeId: " + resumeId);

                    RunState state = status.state();
                    if (state == RunState.DONE) { finalState = RunState.DONE; break; }
                    if (state == RunState.OUT_OF_CREDITS) {
                        finalState = RunState.OUT_OF_CREDITS;
                        outOfCreditsMessage = status.message() != null && !status.message().isBlank()
                                ? status.message() : "Out of credits";
                        break;
                    }
                    if (state == RunState.ERROR) throw new RuntimeException(
                            status.message() != null && !status.message().isBlank()
                                    ? status.message() : "Resume execution failed");
                    if (state == RunState.CANCELLED) throw new RuntimeException("Resume cancelled");

                    Thread.sleep(sleepMs);
                }

                DebugResponseDTO result = debugGateway.getResumeResult(resumeId);
                if (result == null) throw new IllegalStateException("No resume result available");

                if (finalState == RunState.OUT_OF_CREDITS) {
                    throw new DebugResumeOutOfCreditsException(outOfCreditsMessage, result);
                }

                return result;
            }
        };

        task.setOnSucceeded(ev -> presenter.onDebugSnapshot(task.getValue()));
        task.setOnFailed(ev -> handleStepFailure(task.getException()));
        new Thread(task, "debug-resume").start();
    }

    public void stop() {
        String id = sessionId;
        if (id == null) return;
        sessionId = null;

        new Thread(() -> {
            try {
                debugGateway.stop(id);
            } catch (Exception ignored) {}
        }, "debug-stop").start();

        Platform.runLater(() -> isDebugInProgress.set(false));
    }

    private void handleStepFailure(Throwable ex) {
        if (ex instanceof DebugResumeOutOfCreditsException e) {
            // Show partial result, then redirect
            if (e.getPartial() != null) presenter.onDebugSnapshot(e.getPartial());
            presenter.onDebugOutOfCredits(ex.getMessage());
        } else if (ex instanceof DebugOutOfCreditsException) {
            presenter.onDebugOutOfCredits(ex.getMessage());
        } else {
            presenter.onDebugFailed(ex != null ? ex.getMessage() : "Unknown error");
        }
    }
}
