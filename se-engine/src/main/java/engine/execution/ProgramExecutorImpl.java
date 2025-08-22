package engine.execution;

import dto.ProgramApi;
import engine.instruction.Instruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.program.Program;
import engine.variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProgramExecutorImpl implements ProgramExecutor{

    private final Program program;
    ExecutionContext context;
    List<Long> inputsValues;
    int runDegree = 0;
    long result = 0;

    public ProgramExecutorImpl(Program program) {
        this.program = program;
        this.context = new ExecutionContextImpl();
        this.inputsValues = new ArrayList<Long>();
    }

    @Override
    public long run(Long... inputs) {
        Instruction currentInstruction = program.getInstructionsList().get(0);
        Instruction nextInstruction = null;
        Label nextLabel;

        inputsValues = List.of(inputs);
        context.initializeVariables(program, inputs);

        do {
                nextLabel = currentInstruction.execute(context);

                if (nextLabel == FixedLabel.EMPTY) {
                    int indexOfNextInstruction = program.getInstructionsList().indexOf(currentInstruction) + 1;

                    // If there is more instructions, else Exit
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

        return context.getVariableValue(Variable.RESULT);
    }

    @Override
    public Map<Variable, Long> variableState() {
        return context.getVariableState();
    }

    @Override
    public long getVariableValue(Variable variable) {
        return context.getVariableValue(variable);
    }

    @Override
    public String programDisplay() {
        return program.getProgramDisplay();
    }

    @Override
    public ProgramApi getProgramApi() {
        return new ProgramApi(programDisplay());
    }

    @Override
    public int getRunDegree() {
        return runDegree;
    }

    @Override
    public List<Long> getInputsValues() {
        return inputsValues;
    }

    @Override
    public int getTotalCyclesOfProgram() {
        return program.getTotalCyclesOfProgram();
    }

    @Override
    public void extendProgram(int degree) {
        if (degree > 0) {
            program.extendProgram(degree);
        }
    }

    @Override
    public Set<Variable> getInputVariablesOfProgram() {
        return program.getInputVariables();
    }

    @Override
    public int calculateProgramMaxDegree() {
        return program.calculateProgramMaxDegree();
    }
}