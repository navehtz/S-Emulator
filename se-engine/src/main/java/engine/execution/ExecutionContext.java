package engine.execution;

import engine.variable.Variable;

import java.util.Map;

public interface ExecutionContext {
    void initializeVariables(Long... inputs);
    long getVariableValue(Variable variable);
    void updateVariable(Variable variable, long value);
    Map<Variable, Long> getVariableState();
}
