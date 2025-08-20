package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.instruction.LabelReferencesInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class JumpZeroInstruction extends AbstractInstruction implements LabelReferencesInstruction {
    private final Label referencesLabel;

    public JumpZeroInstruction(Variable variable, Label referencesLabel) {
        super(InstructionData.JUMP_ZERO, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY);
        this.referencesLabel = referencesLabel;
    }

    public JumpZeroInstruction(Variable variable, Label label, Label referencesLabel) {
        super(InstructionData.JUMP_ZERO, InstructionType.SYNTHETIC, variable, label);
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(this.getTargetVariable());

        return variableValue == 0 ? this.referencesLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(variableRepresentation);
        command.append(" = 0 GOTO ");
        command.append(referencesLabel.getLabelRepresentation());

        return command.toString();
    }

    @Override
    public Label getReferenceLabel() {
        return referencesLabel;
    }
}
