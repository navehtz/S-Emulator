package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class AssignmentInstruction extends AbstractInstruction {

    private final Variable sourceVariable;

    public AssignmentInstruction(Variable targetVariable, Variable sourceVariable) {
        super(InstructionData.ASSIGNMENT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY);
        this.sourceVariable = sourceVariable;
    }

    public AssignmentInstruction(Variable targetVariable, Label label, Variable sourceVariable) {
        super(InstructionData.ASSIGNMENT, InstructionType.SYNTHETIC, targetVariable, label);
        this.sourceVariable = sourceVariable;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long sourceVariableValue = context.getVariableValue(sourceVariable);
        context.updateVariable(getTargetVariable(), sourceVariableValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String sourceVariableRepresentation = sourceVariable.getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(targetVariableRepresentation);
        command.append(" <- ");
        command.append(sourceVariableRepresentation);

        return command.toString();
    }

    @Override
    public Variable getSourceVariable() {
        return sourceVariable;
    }
}
