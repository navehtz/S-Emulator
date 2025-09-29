package instruction.synthetic.quoteArg;

import execution.ExecutionContext;
import variable.Variable;

import java.io.Serializable;

public class VarArg implements QuoteArg, Serializable {
    private final Variable variable;

    public VarArg(Variable variable) {
        this.variable = variable;
    }


    @Override
    public long eval(ExecutionContext context) {
        return context.getVariableValue(variable);
    }

    @Override
    public String render() {
        return variable.getRepresentation();
    }

    public Variable getVariable() {
        return variable;
    }
}
