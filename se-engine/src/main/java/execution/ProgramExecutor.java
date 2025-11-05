package execution;

import operation.OperationView;
import variable.Variable;

import java.util.List;
import java.util.Map;

public interface ProgramExecutor {

    //void run(int runDegree, Long... inputs);
    void run(String userName, int runDegree, Long... inputs);

    OperationView getProgram();
    long getVariableValue(Variable variable);
    int getRunDegree();
    List<Long> getInputsValuesOfUser();
    int getTotalCyclesOfProgram();
    Map<String, Long> getVariablesToValuesSorted();

    //String getUserName();
    String getArchitectureRepresentation(); // "I"/"II"/"III"/"IV"
    String getOperationName();              // executed program/function name
}
