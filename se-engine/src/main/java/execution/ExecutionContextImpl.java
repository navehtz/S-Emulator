package execution;

import engine.ProgramRegistry;
import operation.Operation;
import operation.OperationInvoker;
import operation.OperationView;
import variable.Variable;
import variable.VariableImpl;
import variable.VariableType;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


public class ExecutionContextImpl implements ExecutionContext, Serializable {

    private final Map<Variable, Long> variableToValue;

    private final ProgramRegistry registry;
    private final OperationInvoker invoker;
    private int lastInvocationCycles = 0;

    public ExecutionContextImpl(ProgramRegistry registry, OperationInvoker invoker) {
        this.variableToValue = new HashMap<>();
        this.registry = registry;
        this.invoker = invoker;
    }

    @Override
    public void initializeVariables(OperationView program, Long... inputs) {
        program.sortVariableSetByNumber(program.getInputVariables());

        initializeInputVariableFromUserInput(program, inputs);
        initializeWorkVariable(program);

        this.updateVariable(Variable.RESULT, 0L);
    }

    private void initializeInputVariableFromUserInput(OperationView program, Long[] inputs) {

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

    private void initializeWorkVariable(OperationView program) {
        for (Variable currWorkVariable : program.getWorkVariables()) {
            this.updateVariable(currWorkVariable, 0L);
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
    public long getOperationResult() {
        return variableToValue.getOrDefault(Variable.RESULT, 0L);
    }

    @Override
    public long invokeOperation(String name, long... args) {
        if (registry == null) {
            throw new IllegalStateException("No ProgramRegistry bound to ExecutionContext");
        }

        OperationView op = registry.getProgramByName(name);
        return invokeOperation(op, args);
    }

    @Override
    public long invokeOperation(OperationView op, long... args) {
        if (registry == null) {
            throw new IllegalStateException("No ProgramRegistry bound to ExecutionContext");
        }

        this.lastInvocationCycles = invoker.getLastCycles();
        return invoker.invokeOperation(op, args);
    }

    @Override
    public int getLastInvocationCycles() {
        return lastInvocationCycles;
    }

}
