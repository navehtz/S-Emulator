package engine.instruction.basic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.instruction.LabelReferencesInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class JumpNotZeroInstruction extends AbstractInstruction implements LabelReferencesInstruction {
    private final Label referencesLabel;

    public JumpNotZeroInstruction(Variable variable, Label referencesLabel) {
        super(InstructionData.JUMP_NOT_ZERO, InstructionType.BASIC ,variable, FixedLabel.EMPTY);
        this.referencesLabel = referencesLabel;
    }

    public JumpNotZeroInstruction(Variable variable, Label label, Label referencesLabel) {
        super(InstructionData.JUMP_NOT_ZERO, InstructionType.BASIC, variable, label);
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(this.getTargetVariable());

        return variableValue != 0 ? this.referencesLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(variableRepresentation);
        command.append(" != 0 GOTO ");
        command.append(referencesLabel.getLabelRepresentation());

        return command.toString();
    }

    @Override
    public Label getReferenceLabel() {
        return referencesLabel;
    }
}
