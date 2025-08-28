package history;

import execution.ProgramExecutor;

import java.util.List;

public interface ExecutionHistory {

    void addProgramToHistory(ProgramExecutor programExecutor);
    List<ProgramExecutor> getProgramsExecutions();
}
