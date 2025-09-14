package dto;

public record InstructionDTO(
    int instructionNumber,
    String instructionTypeStr,
    String labelStr,
    String command,
    int cycleNumber,
    String instructionName,
    String referenceLabelStr,
    String targetVariableStr,
    String sourceVariableStr,
    InstructionDTO origin
) {}



