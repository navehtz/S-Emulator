package dto;

import java.util.List;
import java.util.Map;

public record ProgramExecutorDTO (
    ProgramDTO programDTO,
    Map<String, Long> variablesToValuesSorted,
    long result,
    int totalCycles,
    int degree,
    List<Long> inputsValuesOfUser
) {}
