package debug;

import dto.DebugDTO;
import dto.ProgramExecutorDTO;
import execution.ProgramExecutor;

import java.util.List;

public interface Debug {
    DebugDTO resume(List<Boolean> breakPoints) throws InterruptedException;
    DebugDTO stepOver();
    DebugDTO stepBack();
    DebugDTO stop();
    DebugDTO init();

    boolean hasMoreInstructions();

    //ProgramExecutorDTO buildSnapshotDTO(ProgramExecutor programExecutor);
    //ProgramExecutor getDebugProgramExecutor();
    int getCurrentInstructionIndex();
    int getNextInstructionIndex();
}
