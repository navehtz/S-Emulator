package execution;

import program.Program;
import variable.Variable;

public interface ExecutionContext {
    void initializeVariables(Program program, Long... inputs);
    long getVariableValue(Variable variable);
    void updateVariable(Variable variable, long value);
}
