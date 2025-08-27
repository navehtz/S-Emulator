package execution;

import variable.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProgramExecutor {
    long run(int runDegree, Long... inputs);

    long getVariableValue(Variable variable);
    int getRunDegree();
    List<Long> getInputsValuesOfUser();
    int getTotalCyclesOfProgram();
    long getResultValue();
    Set<Variable> getInputVariablesSet();
    String getVariablesWithValuesSortedString();

    Map<String, Long> getVariablesToValuesSorted();

    //Map<Variable, Long> getInputAndWorkVariablesAndTheirValuesMap();
    //int calculateProgramMaxDegree();
    //String getExtendedProgramDisplay();
    //void extendProgram(int degree);
}
