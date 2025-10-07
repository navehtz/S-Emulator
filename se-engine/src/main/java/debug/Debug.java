package debug;

import dto.DebugDTO;

import java.util.List;

public interface Debug {
    DebugDTO resume(List<Boolean> breakPoints) throws InterruptedException;
    DebugDTO stepOver();
    DebugDTO stepBack();
    DebugDTO stop();
    DebugDTO init();

    boolean hasMoreInstructions();

    int getCurrentInstructionIndex();
    int getNextInstructionIndex();
}
