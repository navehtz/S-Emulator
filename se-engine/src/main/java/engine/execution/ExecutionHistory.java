package engine.execution;

import engine.program.Program;

import java.util.List;

public interface ExecutionHistory {
    boolean hasHistory();
    String displayExecutionHistory();
    void addProgramToHistory(ProgramExecutor programExecutor);

    List<ProgramExecutor> getProgramExecutorsHistory();
}
