package function;

import operation.Operation;
import operation.OperationView;
import variable.Variable;

import java.util.List;
import java.util.Optional;

public interface Function extends OperationView {
    List<Variable> parameters();
    Optional<Variable> returnVariable();

    public String getName();
    public String getUserString();
}
