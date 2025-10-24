package dto.dashboard;

public record UserDTO (
    String userName,
    int numProgramsUploaded,
    int numFunctionsUploaded,
    int currentCredits,
    int usedCredits,
    int numOfExecutions
    ) {}
