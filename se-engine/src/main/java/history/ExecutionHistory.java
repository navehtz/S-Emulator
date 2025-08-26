package history;

import execution.ProgramExecutor;

public interface ExecutionHistory {

    String displayExecutionHistory();
    void addProgramToHistory(ProgramExecutor programExecutor);
    boolean hasHistory();

}
