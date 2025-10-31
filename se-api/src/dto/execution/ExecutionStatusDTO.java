package dto.execution;

public record ExecutionStatusDTO (
    String id,
    RunState state,
    String ownerUser,
    String programName,
    String functionName,
    int progressPercent,
    String message
) {}
