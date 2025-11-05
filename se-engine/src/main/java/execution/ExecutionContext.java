package execution;

import operation.OperationView;
import variable.Variable;

public interface ExecutionContext {
    void initializeVariables(OperationView program, Long... inputs);
    long getVariableValue(Variable v);
    long getOperationResult();
    void updateVariable(Variable v, long value);

    long invokeOperation(String operationName, long... args);
    long invokeOperation(OperationView operation, String userName, long... args);

    int getLastInvocationCycles();
}
