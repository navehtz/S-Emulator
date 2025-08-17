package engine.execution;

import engine.program.Program;
import engine.variable.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExecutionContextImpl implements ExecutionContext{

    private final Map<Variable, Long> variableToValue;

    public ExecutionContextImpl() {
        this.variableToValue = new HashMap<>();
    }

    @Override
    public void initializeVariables(Program program, Long... inputs) {
        Set<Variable> inputVariables = program.getInputVariables();
        int i = 0;

        for (Variable currVariable : inputVariables){
            long value = (i < inputs.length && inputs[i] != null) ? inputs[i] : 0L;
            this.updateVariable(currVariable, value);
            i++;
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
