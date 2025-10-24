package dto.dashboard;

public record UserHistoryRowDTO(
        int ordinal,
        String operationName,
        int architecture,
        int degree,
        int result,
        int totalCycles
) {
}
