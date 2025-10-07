package execution;

import engine.ProgramRegistry;
import instruction.Instruction;
import label.FixedLabel;
import label.Label;
import operation.OperationInvoker;
import operation.OperationView;
import variable.Variable;

import java.io.Serializable;
import java.util.*;

public class ProgramExecutorImpl implements ProgramExecutor, Serializable {

    private final OperationView program;
    private final ProgramRegistry programRegistry;
    private final ExecutionContext context;
    private List<Long> inputsValues;
    private int runDegree = 0;
    private int totalCycles = 0;

    public ProgramExecutorImpl(OperationView program, ProgramRegistry registry) {
        this.program = program;
        this.programRegistry = Objects.requireNonNull(registry, "Program registry cannot be null");
        OperationInvoker invoker = new ProgramExecutorInvoker(registry);
        this.context = new ExecutionContextImpl(registry, invoker);
        this.inputsValues = new ArrayList<>();
    }


    // Called by the invoker the context holds, to execute a callee Operation.
//    private long invokeCallee(OperationView callee, long... args) {
//        ProgramExecutorImpl nestedExecutor = new ProgramExecutorImpl(callee, programRegistry);
//        nestedExecutor.runDegree = this.runDegree;
//
//        Long[] boxedArgs = Arrays.stream(args).boxed().toArray(Long[]::new);
//        nestedExecutor.run(runDegree, boxedArgs);
//
//        return nestedExecutor.getExecutionContext().getOperationResult();
//    }

    public ExecutionContext getExecutionContext() {
        return context;
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
    public OperationView getProgram() {
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