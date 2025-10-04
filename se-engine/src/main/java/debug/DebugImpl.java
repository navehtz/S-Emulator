package debug;

import dto.DebugDTO;
import dto.ProgramExecutorDTO;
import execution.ProgramExecutor;

import java.util.List;

public class DebugImpl implements Debug {

    @Override
    public DebugDTO resume(List<Boolean> breakPoints) throws InterruptedException {
        return null;
    }

    @Override
    public DebugDTO stepOver() {
        return null;
    }

    @Override
    public DebugDTO stepBack() {
        return null;
    }

    @Override
    public DebugDTO stop() {
        return null;
    }

    @Override
    public boolean hasMoreInstructions() {
        return false;
    }

    @Override
    public ProgramExecutorDTO buildProgramExecutorDTO(ProgramExecutor programExecutor) {
        return null;
    }

    @Override
    public ProgramExecutor getDebugProgramExecutor() {
        return null;
    }

    @Override
    public int getCurrentInstructionIndex() {
        return 0;
    }

    @Override
    public int getNextInstructionIndex() {
        return 0;
    }
}
