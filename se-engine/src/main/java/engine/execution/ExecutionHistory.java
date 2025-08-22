package engine.execution;

import engine.program.Program;

public interface ExecutionHistory {
    boolean hasHistory();
    String displayExecutionHistory();
    void addProgramToHistory(ProgramExecutor programExecutor);
}
