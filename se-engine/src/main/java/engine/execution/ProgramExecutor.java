package engine.execution;

import engine.variable.Variable;

import java.util.Map;

public interface ProgramExecutor {
    long run(Long... inputs);
    Map<Variable, Long> variableState();
}
