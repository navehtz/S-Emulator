package ui.execution.debug;

import dto.execution.DebugResponseDTO;

class DebugResumeOutOfCreditsException extends Exception {
    private final DebugResponseDTO partial;

    DebugResumeOutOfCreditsException(String message, DebugResponseDTO partial) {
        super(message);
        this.partial = partial;
    }

    DebugResponseDTO getPartial() {
        return partial;
    }
}
