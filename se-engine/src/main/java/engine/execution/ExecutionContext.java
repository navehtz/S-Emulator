package engine.execution;

import engine.variable.Variable;

public interface ExecutionContext {
    long getVariableValue(Variable variable);
    void updateVariable(Variable variable, long value);
}
