package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class ConstantAssignmentInstruction extends AbstractInstruction {

    private final long constantValue;

    public ConstantAssignmentInstruction(Variable targetVariable, long constantValue) {
        super(InstructionData.CONSTANT_ASSIGNMENT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY);
        this.constantValue = constantValue;
    }

    public ConstantAssignmentInstruction(Variable targetVariable, Label label, long constantValue) {
        super(InstructionData.CONSTANT_ASSIGNMENT, InstructionType.SYNTHETIC, targetVariable, label);
        this.constantValue = constantValue;
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getTargetVariable(), constantValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(constantValue);

        return command.toString();
    }
}
