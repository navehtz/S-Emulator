package dto.execution;

public record ExecutionStatusDTO (
    String id,
    String programName,
    String userName,
    RunState state,
    int progressPercent,
    String message
) {}
