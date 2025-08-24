package history;

import execution.ProgramExecutor;

import java.util.List;

public interface ExecutionHistory {
    boolean hasHistory();
    String displayExecutionHistory();
    void addProgramToHistory(ProgramExecutor programExecutor);

    List<ProgramExecutor> getProgramExecutorsHistory();
}
