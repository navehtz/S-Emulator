package dto.execution;

import java.util.List;

public record DebugStartRequestDTO(
        String programName,
        String architecture,
        int degree,
        List<Long> inputs) {
}
