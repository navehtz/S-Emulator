package ui.execution.run;

import dto.execution.ProgramExecutorDTO;

class OutOfCreditsException extends RuntimeException {
    private final ProgramExecutorDTO partialResult;

    OutOfCreditsException(String message, ProgramExecutorDTO partialResult) {
        super(message);
        this.partialResult = partialResult;
    }

    ProgramExecutorDTO getPartialResult() {
        return partialResult;
    }
}