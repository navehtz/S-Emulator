package execution;

import program.Program;
import variable.Variable;

import java.util.Map;

public interface ExecutionContext {
    void initializeVariables(Program program, Long... inputs);
    long getVariableValue(Variable variable);
    void updateVariable(Variable variable, long value);
    Map<Variable, Long> getVariableState();
}
