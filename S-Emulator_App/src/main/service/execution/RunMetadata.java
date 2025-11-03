package main.service.execution;

public record RunMetadata(
        String runId,
        String programName,
        String username
) {
}
