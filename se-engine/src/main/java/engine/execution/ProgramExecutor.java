package engine.execution;

import dto.ProgramApi;
import engine.variable.Variable;

import java.util.Map;

public interface ProgramExecutor {
    long run(Long... inputs);
    Map<Variable, Long> variableState();

    String programDisply();

    ProgramApi getProgramApi();
}
