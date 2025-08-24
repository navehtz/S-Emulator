package execution;

import variable.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProgramExecutor {
    long run(Long... inputs);
    Map<Variable, Long> variableState();        // TODO: ask aviad why needed and delete

    long getVariableValue(Variable variable);
    int getRunDegree();
    //String getProgramDisplay();
    //String getExtendedProgramDisplay();
    List<Long> getInputsValues();
    int getTotalCyclesOfProgram();
    //Set<Variable> getInputVariablesSet();
    //Map<Variable, Long> getInputAndWorkVariablesAndTheirValuesMap();
    //String getInputAndWorkVariablesWithValuesDisplay();

    //void extendProgram(int degree);
    //int calculateProgramMaxDegree();
}
