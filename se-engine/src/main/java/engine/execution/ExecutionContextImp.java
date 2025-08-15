package engine.execution;

import engine.variable.Variable;
import engine.variable.VariableImp;
import engine.variable.VariableType;

import java.util.HashMap;
import java.util.Map;

public class ExecutionContextImp implements ExecutionContext{

    private final Map<Variable, Long> variableToValue;

    public ExecutionContextImp() {
        this.variableToValue = new HashMap<>();
    }

    @Override
    public void initializeVariables(Long... inputs) {
        for (int i = 0; i < inputs.length; i++) {
            Variable newVariable = new VariableImp(VariableType.INPUT, (i + 1));
            this.updateVariable(newVariable, inputs[i]);
        }

        this.updateVariable(Variable.RESULT, 0L);
    }

    @Override
    public long getVariableValue(Variable variable) {
        return variableToValue.get(variable);
    }

    @Override
    public void updateVariable(Variable variable, long value) {
        variableToValue.put(variable, value);
    }

    public Map<Variable, Long> getVariableState() {
        return this.variableToValue;
    }
}
