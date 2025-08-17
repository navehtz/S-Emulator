package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class GotoLabelInstruction extends AbstractInstruction {

    private final Label addedLabel;

    public GotoLabelInstruction(Variable variable, Label addedLabel) {
        super(InstructionData.GOTO_LABEL, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY);
        this.addedLabel = addedLabel;
    }

    public GotoLabelInstruction(Variable variable, Label label, Label addedLabel) {
        super(InstructionData.GOTO_LABEL, InstructionType.SYNTHETIC, variable, label);
        this.addedLabel = addedLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        return addedLabel;
    }

    @Override
    public String getCommand() {
        String labelRepresentation = addedLabel.getLabelRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("GOTO ");
        command.append(labelRepresentation);

        return command.toString();
    }
}
