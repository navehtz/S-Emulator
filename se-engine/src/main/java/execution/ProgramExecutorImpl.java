package execution;

import instruction.Instruction;
import label.FixedLabel;
import label.Label;
import operation.Operation;
import variable.Variable;

import java.io.Serializable;
import java.util.*;

public class ProgramExecutorImpl implements ProgramExecutor, Serializable {

    private final Operation program;
    private final ExecutionContext context;
    private List<Long> inputsValues;
    private int runDegree = 0;
    private int totalCycles = 0;


    public ProgramExecutorImpl(Operation program) {
        this.program = program;
        this.context = new ExecutionContextImpl();
        this.inputsValues = new ArrayList<>();
    }

    @Override
    public void run(int runDegree, Long... inputs) {
        Instruction currentInstruction = program.getInstructionsList().getFirst();
        Instruction nextInstruction = null;
        Label nextLabel;

        inputsValues = List.of(inputs);
        context.initializeVariables(program, inputs);
        this.runDegree = runDegree;

        do {
            nextLabel = currentInstruction.execute(context);
            totalCycles += currentInstruction.getCycleOfInstruction();

            if (nextLabel == FixedLabel.EMPTY) {
                int indexOfNextInstruction = program.getInstructionsList().indexOf(currentInstruction) + 1;

                // If there are more instructions, else Exit
                if (indexOfNextInstruction < program.getInstructionsList().size()) {
                    nextInstruction = program.getInstructionsList().get(indexOfNextInstruction);
                } else {
                    nextLabel = FixedLabel.EXIT;
                }
            } else if (nextLabel != FixedLabel.EXIT) {
                nextInstruction = program.getInstructionByLabel(nextLabel);
            }

            currentInstruction = nextInstruction;

        } while(nextLabel != FixedLabel.EXIT);

        context.getVariableValue(Variable.RESULT);
    }

    @Override
    public Operation getProgram() {
        return program;
    }

    @Override
    public long getVariableValue(Variable variable) {
        return context.getVariableValue(variable);
    }

    @Override
    public int getRunDegree() {
        return runDegree;
    }

    @Override
    public List<Long> getInputsValuesOfUser() {
        return inputsValues;
    }

    @Override
    public int getTotalCyclesOfProgram() {
        return this.totalCycles;
    }

    @Override
    public Map<String, Long> getVariablesToValuesSorted() {
        Map<String, Long> VariablesToValuesSorted = new LinkedHashMap<>();

        for (Variable v : program.getInputAndWorkVariablesSortedBySerial()) {
            VariablesToValuesSorted.put(v.getRepresentation(), context.getVariableValue(v));
        }

        return VariablesToValuesSorted;
    }
}