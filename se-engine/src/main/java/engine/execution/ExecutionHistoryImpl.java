package engine.execution;

import engine.variable.Variable;

import java.util.ArrayList;
import java.util.List;


public class ExecutionHistoryImpl implements ExecutionHistory {

    private final List<ProgramExecutor> runs = new ArrayList<>();

    @Override
    public boolean hasHistory() {
        return !runs.isEmpty();
    }

    @Override
    public String displayExecutionHistory() {
        StringBuilder executionHistory = new StringBuilder().append("Execution History:").append(System.lineSeparator());

        for(int i = 0; i < runs.size(); i++) {
            ProgramExecutor programExecutor = runs.get(i);

            executionHistory.append("Program #").append(i + 1).append(System.lineSeparator());
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
        runs.add(programExecutor);
    }

}
