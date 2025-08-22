package engine.instruction.basic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.Instruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.instruction.synthetic.ZeroVariableInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class NoOpInstruction extends AbstractInstruction {

    public NoOpInstruction(Variable variable) {
        super(InstructionData.NO_OP, InstructionType.BASIC ,variable, FixedLabel.EMPTY);
    }

    public NoOpInstruction(Variable variable, Label label) {
        super(InstructionData.NO_OP, InstructionType.BASIC, variable, label);
    }

    @Override
    public Instruction createNewInstructionWithNewLabel(Label newLabel) {
        return new NoOpInstruction(getTargetVariable(), newLabel);
    }

    @Override
    public Label execute(ExecutionContext context) {
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(variableRepresentation);

        return command.toString();
    }
}
