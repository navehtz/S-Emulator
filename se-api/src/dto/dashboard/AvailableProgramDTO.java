package dto.dashboard;

public record AvailableProgramDTO(
        String programName,
        String userUploaded,
        int numOfInstructions,
        int maxDegree,
        int numOfExecutions,
        int averageCost
) {
}
