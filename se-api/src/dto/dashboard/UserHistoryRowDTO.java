package dto.dashboard;

import java.util.List;
import java.util.Map;

public record UserHistoryRowDTO(
        int ordinal,
        String programType,
        String operationName,
        int architecture,
        int degree,
        int result,
        int totalCycles,
        Map<String, Long> variablesToValuesSorted,
        List<Long> inputsValuesOfUser
) {
}
