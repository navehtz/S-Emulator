package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class JumpEqualVariableInstruction extends AbstractInstruction {

    private final Label addedLabel;
    private final Variable sourceVariable;

    public JumpEqualVariableInstruction(Variable targetVariable, Variable sourceVariable, Label addedLabel) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY);
        this.sourceVariable = sourceVariable;
        this.addedLabel = addedLabel;
    }

    public JumpEqualVariableInstruction(Variable targetVariable, Label label, Variable sourceVariable, Label addedLabel) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, InstructionType.SYNTHETIC, targetVariable, label);
        this.sourceVariable = sourceVariable;
        this.addedLabel = addedLabel;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long targetVariableValue = context.getVariableValue(getTargetVariable());
        long sourceVariableValue = context.getVariableValue(sourceVariable);

        return (targetVariableValue == sourceVariableValue) ? addedLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String sourceVariableRepresentation = sourceVariable.getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(targetVariableRepresentation);
        command.append(" = ");
        command.append(sourceVariableRepresentation);
        command.append(" GOTO ");
        command.append(addedLabel.getLabelRepresentation());

        return command.toString();
    }

    @Override
    public Variable getSourceVariable() {
        return sourceVariable;
    }
}
