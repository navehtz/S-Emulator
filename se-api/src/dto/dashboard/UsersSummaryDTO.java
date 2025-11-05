package dto.dashboard;

import java.util.List;

public record UsersSummaryDTO(
        List<UserDTO> users
) {
}
