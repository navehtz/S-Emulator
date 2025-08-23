package engine.execution;

import engine.program.Program;
import engine.variable.Variable;
import engine.variable.VariableImpl;
import engine.variable.VariableType;

import java.util.*;
import java.util.stream.Collectors;


public class ExecutionContextImpl implements ExecutionContext{

    private final Map<Variable, Long> variableToValue;

    public ExecutionContextImpl() {
        this.variableToValue = new HashMap<>();
    }

    @Override
    public void initializeVariables(Program program, Long... inputs) {
        program.sortInputVariablesByTypeThenNumber();

        initializeInputVariable(program, inputs);
        initializeWorkVariable(program);

        this.updateVariable(Variable.RESULT, 0L);
    }

    private void initializeInputVariable(Program program, Long[] inputs) {

        Set<Variable> inputVariables = program.getInputVariables();

        Map<Integer, Variable> byNum = inputVariables.stream()
                .collect(Collectors.toMap(Variable::getNumber, v -> v));


        for (int i = 1; i <= inputs.length; i++) {
            Variable variableSerialI = byNum.get(i);
            if (variableSerialI == null) {
                variableSerialI = new VariableImpl(VariableType.INPUT, i);
                program.addInputVariable(variableSerialI);   // add to the set of inputs
                byNum.put(i, variableSerialI);
            }
            long value = inputs[i - 1] != null ? inputs[i - 1] : 0L;
            this.updateVariable(variableSerialI, value);
        }

        for (Variable v : inputVariables) {     // For all the variables that their serial number is bigger that inputs.length
            if (v.getNumber() > inputs.length) {
                this.updateVariable(v, 0L);
            }
        }
    }

/*    private void initializeInputVariable(Program program, Long[] inputs) {
        Set<Variable> inputVariables = program.getInputVariables();
        int i = 0;

        for (Variable currInputVariable : inputVariables){
            int serialNumberOfCurrInputVariable = currInputVariable.getNumber();
            long value = 0;

            if (serialNumberOfCurrInputVariable - 1 < inputs.length) {      // the inputs strat from 1 (x1, x2..), not from zero
                value = inputs[serialNumberOfCurrInputVariable - 1];
            }

            this.updateVariable(currInputVariable, value);
        }
    }*/

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
