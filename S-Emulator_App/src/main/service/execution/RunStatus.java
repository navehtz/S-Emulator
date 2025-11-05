package main.service.execution;

import dto.execution.ExecutionStatusDTO;

import java.util.concurrent.Future;

public record RunStatus(
    String runId,
    String programName,
    String username,
    ExecutionStatus executionStatus,
    Future<?> executionFuture
) {

    public ExecutionStatusDTO toDTO() {
        var executionStatusSnapshot = executionStatus.getAtomicSnapshot();
        return new ExecutionStatusDTO(
                runId(),
                programName(),
                username(),
                executionStatusSnapshot.state(),
                executionStatusSnapshot.progressPercent(),
                executionStatusSnapshot.message()
        );
    }
}
