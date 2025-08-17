package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class JumpZeroInstruction extends AbstractInstruction {
    private final Label addedLabel;

    public JumpZeroInstruction(Variable variable, Label addedLabel) {
        super(InstructionData.JUMP_ZERO, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY);
        this.addedLabel = addedLabel;
    }

    public JumpZeroInstruction(Variable variable, Label label, Label addedLabel) {
        super(InstructionData.JUMP_ZERO, InstructionType.SYNTHETIC, variable, label);
        this.addedLabel = addedLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        return null;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(variableRepresentation);
        command.append(" = 0 GOTO ");
        command.append(addedLabel.getLabelRepresentation());

        return command.toString();
    }
}
