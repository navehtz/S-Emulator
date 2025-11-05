package dto.execution;

import java.util.List;

public record ProgramRunRequestDTO(
        String programName,
        int degree,
        String architecture,
        String userName,
        List<Long> inputsValuesOfUser
) {
}
