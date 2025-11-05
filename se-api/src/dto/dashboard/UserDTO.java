package dto.dashboard;

public record UserDTO (
    String userName,
    int numProgramsUploaded,
    int numFunctionsUploaded,
    long currentCredits,
    long usedCredits,
    int numOfExecutions
    ) {}
