package dto.execution;

import java.util.List;

public record ProgramDTO (
    String programName,
    List<String> labelsStr,
    List<String> inputVariables,
    InstructionsDTO instructions,
    List<List<InstructionDTO>> expandedProgram,
    List<String> allVariables
) {}
