package execution;

import operation.Operation;
import operation.OperationView;
import program.Program;
import variable.Variable;

public interface ExecutionContext {
    void initializeVariables(OperationView program, Long... inputs);
    long getVariableValue(Variable v);
    long getOperationResult();
    void updateVariable(Variable v, long value);

    long invokeOperation(String name, long... args);
    long invokeOperation(OperationView operation, long... args);

    int getLastInvocationCycles();
}
