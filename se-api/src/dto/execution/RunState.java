package dto.execution;

public enum RunState {
    PENDING,
    IN_PROGRESS,
    DONE,
    CANCELLED,
    ERROR,
    OUT_OF_CREDITS,
}
