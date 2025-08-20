package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.instruction.LabelReferencesInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class GotoLabelInstruction extends AbstractInstruction implements LabelReferencesInstruction {

    private final Label referencesLabel;

    public GotoLabelInstruction(Variable variable, Label referencesLabel) {
        super(InstructionData.GOTO_LABEL, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY);
        this.referencesLabel = referencesLabel;
    }

    public GotoLabelInstruction(Variable variable, Label label, Label referencesLabel) {
        super(InstructionData.GOTO_LABEL, InstructionType.SYNTHETIC, variable, label);
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        return referencesLabel;
    }

    @Override
    public String getCommand() {
        String labelRepresentation = referencesLabel.getLabelRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("GOTO ");
        command.append(labelRepresentation);

        return command.toString();
    }

    @Override
    public Label getReferenceLabel() {
        return referencesLabel;
    }
}
