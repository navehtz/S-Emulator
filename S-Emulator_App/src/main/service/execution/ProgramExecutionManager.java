package main.service.execution;

import dto.execution.ExecutionStatusDTO;
import dto.execution.ProgramRunRequestDTO;
import dto.execution.RunState;
import engine.Engine;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class ProgramExecutionManager {

    private static final ProgramExecutionManager INSTANCE = new ProgramExecutionManager();
    public static ProgramExecutionManager getInstance() { return INSTANCE; }

    private final ExecutorService runExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final Map<String, RunHandle> executionsStatusMap = new ConcurrentHashMap<>();

    private ProgramExecutionManager() {}

    public ExecutionStatusDTO getExecutionStatus(String runId) {
        RunHandle runHandle = executionsStatusMap.get(runId);
        if (runHandle == null) {
            return null;
        }
        return runHandle.toDTO();
    }

    public String submitRun(ProgramRunRequestDTO request, Engine engine) {
        final String runId = UUID.randomUUID().toString();
        final String programName = request.programName();
        final String userName    = request.userName();
        final String architecture = request.architecture();
        final int degree         = request.degree();
        final List<Long> inputs  = (request.inputsValuesOfUser() == null) ?
                        List.of() :
                        List.copyOf(request.inputsValuesOfUser());

        final ExecutionStatus executionStatus = new ExecutionStatus();
        executionStatus.setState(RunState.PENDING);

        Future<?> future = runExecutor.submit(() -> {
            executionStatus.setState(RunState.IN_PROGRESS);
            try {
                Long[] inputArray = inputs.toArray(new Long[0]);

                // TODO: report progress, wire a callback to executionStatus.setProgressPercent(...)
                engine.runProgram(programName, architecture, degree, userName, inputArray);

                executionStatus.setState(RunState.DONE);
                executionStatus.setMessage("");
            } catch (CancellationException cancellationException) {
                executionStatus.setMessage("Canceled by user");
                executionStatus.setState(RunState.CANCELLED);
                throw cancellationException;
            } catch (Throwable t) {
                executionStatus.setMessage(t.getMessage() == null ? "Execution failed" : t.getMessage());
                executionStatus.setState(RunState.ERROR);
            }
        });

        var metadata = new RunMetadata(runId, programName, userName);
        executionsStatusMap.put(runId, new RunHandle(metadata, executionStatus, future));
        return runId;
    }

    public boolean cancelRun(String runId) {
        RunHandle runHandle = executionsStatusMap.get(runId);
        if (runHandle == null) return false;

        boolean ok = runHandle.executionFuture().cancel(true); // interrupts the virtual thread
        if (ok) {
            runHandle.executionStatus().setMessage("Canceled by user");
            runHandle.executionStatus().setState(RunState.CANCELLED);
        }
        return ok;
    }

    public void updateProgress(String runId, int percent) {
        RunHandle runHandle = executionsStatusMap.get(runId);
        if (runHandle != null) {
            runHandle.executionStatus().setProgressPercent(percent);
        }
    }

    public void shutdown() {
        runExecutor.shutdownNow();
    }


}
