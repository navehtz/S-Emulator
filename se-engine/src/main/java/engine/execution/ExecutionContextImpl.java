package engine.execution;

import engine.program.Program;
import engine.variable.Variable;
import engine.variable.VariableType;

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

        initializeInputVariable(program, inputs);
        initializeWorkVariable(program);

        this.updateVariable(Variable.RESULT, 0L);
    }

    private void initializeInputVariable(Program program, Long[] inputs) {
        Set<Variable> inputVariables = program.getInputVariables();
        int i = 0;

        for (Variable currInputVariable : inputVariables){
            long value = (i < inputs.length && inputs[i] != null) ? inputs[i] : 0L;
            this.updateVariable(currInputVariable, value);
            i++;
        }
    }

    private void initializeWorkVariable(Program program) {
        Set<Variable> workVariables = program.getWorkVariables();

        for (Variable currWorkVariable : workVariables){
            long value = 0L;
            this.updateVariable(currWorkVariable, value);
        }
    }

    @Override
    public long getVariableValue(Variable variable) {
        if(variable.getType() == VariableType.RESULT)
            variable = Variable.RESULT;

        return variableToValue.get(variable);
    }

    @Override
    public void updateVariable(Variable variable, long value) {
        if(variable.getType() == VariableType.RESULT)
            variable = Variable.RESULT;

        variableToValue.put(variable, value);
    }

    public Map<Variable, Long> getVariableState() {
        return this.variableToValue;
    }
}
