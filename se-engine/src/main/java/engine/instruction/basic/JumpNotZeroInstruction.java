package engine.instruction.basic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class JumpNotZeroInstruction extends AbstractInstruction {
    private final Label addedLabel;

    public JumpNotZeroInstruction(Variable variable, Label addedLabel) {
        super(InstructionData.JUMP_NOT_ZERO, InstructionType.BASIC ,variable, FixedLabel.EMPTY);
        this.addedLabel = addedLabel;
    }

    public JumpNotZeroInstruction(Variable variable, Label label, Label addedLabel) {
        super(InstructionData.JUMP_NOT_ZERO, InstructionType.BASIC, variable, label);
        this.addedLabel = addedLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(this.getTargetVariable());

        return variableValue != 0 ? this.addedLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(variableRepresentation);
        command.append(" != 0 GOTO ");
        command.append(addedLabel.getLabelRepresentation());

        return command.toString();
    }
}
