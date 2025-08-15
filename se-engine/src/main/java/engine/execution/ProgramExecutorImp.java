package engine.execution;

import engine.instruction.Instruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.program.Program;
import engine.variable.Variable;
import java.util.Map;

public class ProgramExecutorImp implements ProgramExecutor{

    private final Program program;
    ExecutionContext context;

    public ProgramExecutorImp(Program program) {
        this.program = program;
        this.context = new ExecutionContextImp();
    }

    @Override
    public long run(Long... inputs) {
        Instruction currentInstruction = program.getInstructionsList().get(0);
        Label nextLabel;

        context.initializeVariables(inputs);

        do {
            nextLabel = currentInstruction.execute(context);

            if(nextLabel == FixedLabel.EMPTY) {
                int indexOfNextInstruction = program.getInstructionsList().indexOf(currentInstruction) + 1;
                currentInstruction = program.getInstructionsList().get(indexOfNextInstruction);
            }
            else if(nextLabel != FixedLabel.EXIT) {
                currentInstruction = program.getInstructionByLabel(nextLabel);
            }

        } while(nextLabel != FixedLabel.EXIT);

        return context.getVariableValue(Variable.RESULT);
    }

    @Override
    public Map<Variable, Long> variableState() {
        return context.getVariableState();
    }
}
