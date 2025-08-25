package history;

import execution.ProgramExecutor;

import java.util.List;

public interface ExecutionHistory {

    String displayExecutionHistory();
    void addProgramToHistory(ProgramExecutor programExecutor);

}
