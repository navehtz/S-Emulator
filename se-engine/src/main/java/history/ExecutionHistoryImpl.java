package history;

import execution.ProgramExecutor;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;


public class ExecutionHistoryImpl implements ExecutionHistory {

    private final List<ProgramExecutor> programExecutorsHistory = new ArrayList<>();

    @Override
    public boolean hasHistory() {
        return !programExecutorsHistory.isEmpty();
    }

    @Override
    public String displayExecutionHistory() {
        StringBuilder executionHistory = new StringBuilder().append("Execution History:").append(System.lineSeparator());

        for(int i = 0; i < programExecutorsHistory.size(); i++) {
            ProgramExecutor programExecutor = programExecutorsHistory.get(i);

            executionHistory.append("Run number #").append(i + 1).append(System.lineSeparator());
            executionHistory.append("Run degree: ").append(programExecutor.getRunDegree()).append(System.lineSeparator());
            executionHistory.append("Inputs values: ").append(programExecutor.getInputsValues()).append(System.lineSeparator());
            executionHistory.append("Result: ").append(programExecutor.getVariableValue(Variable.RESULT)).append(System.lineSeparator());
            executionHistory.append("Cycles: ").append(programExecutor.getTotalCyclesOfProgram()).append(System.lineSeparator());
            executionHistory.append(System.lineSeparator());
        }

        return executionHistory.toString();
    }

    @Override
    public void addProgramToHistory(ProgramExecutor programExecutor) {
        programExecutorsHistory.add(programExecutor);
    }

    @Override
    public List<ProgramExecutor> getProgramExecutorsHistory() {
        return programExecutorsHistory;
    }

}
