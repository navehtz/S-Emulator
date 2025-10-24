package dto.dashboard;

public record AvailableFunctionDTO(
        String functionName,
        String mainProgramName,
        String userUploaded,
        int numOfInstructions,
        int maxDegree
) {
}
