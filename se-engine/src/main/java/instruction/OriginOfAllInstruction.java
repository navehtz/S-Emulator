package instruction;

import execution.ExecutionContext;
import label.Label;
import variable.Variable;


public class OriginOfAllInstruction extends AbstractInstruction {


    public OriginOfAllInstruction() {
        super(InstructionData.ORIGIN, InstructionType.BASIC, Variable.RESULT, null, 0);
    }

    @Override
    public String getCommand() {
        return "";
    }

    @Override
    public Label execute(ExecutionContext context) {
        return null;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return null;
    }
}
