package engine.execution;

import engine.variable.Variable;

import java.util.Map;

public class ProgramExecutorImp implements ProgramExecutor{
    @Override
    public long run(Long... inputs) {
        return 0;
    }

    @Override
    public Map<Variable, Long> variableState() {
        return Map.of();
    }
}
