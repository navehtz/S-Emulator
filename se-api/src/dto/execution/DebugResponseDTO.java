package dto.execution;

public record DebugResponseDTO(
        String sessionId,
        DebugDTO snapshot,
        long creditsRemaining) {
}
