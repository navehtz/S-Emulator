package execution;

import operation.Operation;
import program.Program;
import variable.Variable;

public interface ExecutionContext {
    void initializeVariables(Operation program, Long... inputs);
    long getVariableValue(Variable v);
    long getOperationResult();
    void updateVariable(Variable v, long value);

    long invokeOperation(String name, long... args);
    long invokeOperation(Operation operation, long... args);
}
