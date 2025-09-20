package execution;

import operation.Operation;
import program.Program;
import variable.Variable;

public interface ExecutionContext {
    void initializeVariables(Operation program, Long... inputs);
    long getVariableValue(Variable v);
    long getOperationResult(Operation operation);
    void updateVariable(Variable v, long value);
}
