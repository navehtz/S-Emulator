package main.service.execution;

import dto.execution.ExecutionStatusDTO;

import java.util.concurrent.Future;

public record RunHandle(
    RunMetadata runMetadata,
    ExecutionStatus executionStatus,
    Future<?> executionFuture
) {

    public ExecutionStatusDTO toDTO() {
        var executionStatusSnapshot = executionStatus.getAtomicSnapshot();
        return new ExecutionStatusDTO(
                runMetadata.runId(),
                runMetadata.programName(),
                runMetadata.username(),
                executionStatusSnapshot.state(),
                executionStatusSnapshot.progressPercent(),
                executionStatusSnapshot.message()
        );
    }
}
