package function;

import operation.Operation;
import variable.Variable;

import java.util.List;
import java.util.Optional;

public interface Function {
    List<Variable> parameters();
    Optional<Variable> returnVariable();
}
