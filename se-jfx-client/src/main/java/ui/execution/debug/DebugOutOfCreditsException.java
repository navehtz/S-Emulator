package ui.execution.debug;

import java.io.IOException;

class DebugOutOfCreditsException extends IOException {
    DebugOutOfCreditsException(String message) {
        super(message);
    }
}
