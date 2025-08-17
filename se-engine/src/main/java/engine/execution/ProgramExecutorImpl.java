package engine.execution;

import dto.ProgramApi;
import engine.instruction.Instruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.program.Program;
import engine.variable.Variable;

import java.util.Map;

public class ProgramExecutorImpl implements ProgramExecutor{

    private final Program program;
    ExecutionContext context;

    public ProgramExecutorImpl(Program program) {
        this.program = program;
        this.context = new ExecutionContextImpl();
    }

    @Override
    public long run(Long... inputs) {
        Instruction currentInstruction = program.getInstructionsList().get(0);
        Instruction nextInstruction = null;
        Label nextLabel;

        context.initializeVariables(program, inputs);

        do {
            nextLabel = currentInstruction.execute(context);

            if(nextLabel == FixedLabel.EMPTY) {
                int indexOfNextInstruction = program.getInstructionsList().indexOf(currentInstruction) + 1;

                // If there is more instructions, else Exit
                if(indexOfNextInstruction < program.getInstructionsList().size()) {
                    nextInstruction = program.getInstructionsList().get(indexOfNextInstruction);
                }
                else {
                    nextLabel = FixedLabel.EXIT;
                }
            }
            else if(nextLabel != FixedLabel.EXIT) {
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
    public String programRepresentation() {
        return program.programRepresentation();
    }

    @Override
    public ProgramApi getProgramApi() {
        return new ProgramApi(programRepresentation());
    }
}