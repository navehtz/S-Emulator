package dto;

import java.util.List;

public class ProgramDTO {
    private final String programName;
    private final List<String> LabelsStr;
    private final List<String> inputVariables;
    private final InstructionsDTO instructions;
    private final List<List<String>> expandedProgram;

    public ProgramDTO(
            String programName,
            List<String> LabelsStr,
            List<String> inputVariables,
            InstructionsDTO instructions,
            List<List<String>> expandedProgram
    ) {
        this.programName = programName;
        this.LabelsStr = LabelsStr;
        this.inputVariables = inputVariables;
        this.instructions = instructions;
        this.expandedProgram = expandedProgram;
    }

    public String getProgramName() {
        return programName;
    }

    public List<String> getLabelsStr() {
        return LabelsStr;
    }

    public List<String> getInputVariables() {
        return inputVariables;
    }

    public InstructionsDTO getInstructions() {
        return instructions;
    }

    public List<List<String>> getExpandedProgram() {
        return expandedProgram;
    }
}
