package execution;

import operation.Operation;
import program.Program;
import variable.Variable;
import variable.VariableImpl;
import variable.VariableType;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


public class ExecutionContextImpl implements ExecutionContext, Serializable {

    private final Map<Variable, Long> variableToValue;

    public ExecutionContextImpl() {
        this.variableToValue = new HashMap<>();
    }

    @Override
    public void initializeVariables(Operation program, Long... inputs) {
        program.sortVariableSetByNumber(program.getInputVariables());

        initializeInputVariableFromUserInput(program, inputs);
        initializeWorkVariable(program);

        this.updateVariable(Variable.RESULT, 0L);
    }

    private void initializeInputVariableFromUserInput(Operation program, Long[] inputs) {

        Set<Variable> inputVariables = program.getInputVariables();

        Map<Integer, Variable> serialNumberToVariable = inputVariables.stream()
                .collect(Collectors.toMap(Variable::getIndex, v -> v));


        for (int i = 1; i <= inputs.length; i++) {
            Variable variableSerialI = serialNumberToVariable.get(i);

            if (variableSerialI == null) {
                variableSerialI = new VariableImpl(VariableType.INPUT, i);
                program.addInputVariable(variableSerialI);   // add to the set of inputs
                serialNumberToVariable.put(i, variableSerialI);
            }

            long value = inputs[i - 1] != null ? inputs[i - 1] : 0L;
            this.updateVariable(variableSerialI, value);
        }

        for (Variable v : inputVariables) {     // For all the variables that their serial number is bigger that inputs.length
            if (v.getIndex() > inputs.length) {
                this.updateVariable(v, 0L);
            }
        }
    }

    private void initializeWorkVariable(Operation program) {
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

    @Override
    public long getOperationResult(operation.Operation operation) {
        return variableToValue.get(Variable.RESULT);
    }
}
