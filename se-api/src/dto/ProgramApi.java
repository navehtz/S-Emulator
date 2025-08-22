package dto;

public class ProgramApi {
    private final String programRepresentation;

    public ProgramApi(String programRepresentation) {
        this.programRepresentation = programRepresentation;
    }

    public String getProgramDisplay() {
        return programRepresentation;
    }
}
