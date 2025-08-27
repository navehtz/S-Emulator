package history;

import execution.ProgramExecutor;

import java.util.ArrayList;
import java.util.List;


public class ExecutionHistoryImpl implements ExecutionHistory {

    private final List<ProgramExecutor> programExecutorsHistory = new ArrayList<>();

    @Override
    public List<ProgramExecutor> getProgramsExecutions() {
        return programExecutorsHistory;
    }

    @Override
    public void addProgramToHistory(ProgramExecutor programExecutor) {
        programExecutorsHistory.add(programExecutor);
    }
}
