package dto;

import java.util.Map;

public record DebugDTO(
        String programName,
        int currentInstructionNumber,
        int nextInstructionNumber,
        boolean hasMoreInstructions,
        String targetVariable,
        int degree,
        long result,
        int totalCycles,
        Map<String, Long> variablesToValuesSorted) {
}
