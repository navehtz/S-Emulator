package execution;

import program.Program;
import variable.Variable;

public interface ExecutionContext {
    void initializeVariables(Program program, Long... inputs);
    long getVariableValue(Variable v);
    void updateVariable(Variable v, long value);
}
