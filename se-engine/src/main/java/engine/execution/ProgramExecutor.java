package engine.execution;

import dto.ProgramApi;
import engine.program.Program;
import engine.variable.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProgramExecutor {
    long run(Long... inputs);
    Map<Variable, Long> variableState();        // TODO: ask aviad why needed and delete
    String programDisplay();

    long getVariableValue(Variable variable);
    ProgramApi getProgramApi();
    int getRunDegree();
    List<Long> getInputsValues();
    int getTotalCyclesOfProgram();
    Set<Variable> getInputVariablesOfProgram();
    //String getInputAndWorkVariablesAndTheirValues();

    void extendProgram(int degree);
    int calculateProgramMaxDegree();
}
