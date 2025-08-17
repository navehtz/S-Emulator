package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class JumpEqualConstantInstruction extends AbstractInstruction {

    private final Label addedLabel;
    private final long constantValue;

    public JumpEqualConstantInstruction(Variable targetVariable, long constantValue, Label addedLabel) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY);
        this.constantValue = constantValue;
        this.addedLabel = addedLabel;
    }

    public JumpEqualConstantInstruction(Variable targetVariable, Label label, long constantValue, Label addedLabel) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, InstructionType.SYNTHETIC, targetVariable, label);
        this.constantValue = constantValue;
        this.addedLabel = addedLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getTargetVariable());

        return (variableValue == constantValue) ? addedLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(variableRepresentation);
        command.append(" = ");
        command.append(constantValue);
        command.append(" GOTO ");
        command.append(addedLabel.getLabelRepresentation());

        return command.toString();
    }
}
