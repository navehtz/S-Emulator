package dto;

public record InstructionDTO(
    String instructionName,
    int instructionNumber,
    int cycleNumber,
    String instructionTypeStr,
    String labelStr,
    String referenceLabelStr,
    String targetVariableStr,
    String sourceVariableStr,
    String command,
    InstructionDTO origin
) {}



