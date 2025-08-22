package engine.instruction.basic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.Instruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.label.Label;
import engine.label.FixedLabel;
import engine.variable.Variable;

public class DecreaseInstruction extends AbstractInstruction {

    public DecreaseInstruction(Variable variable) {
        super(InstructionData.DECREASE, InstructionType.BASIC, variable, FixedLabel.EMPTY);
    }

    public DecreaseInstruction(Variable variable, Label label) {
        super(InstructionData.DECREASE, InstructionType.BASIC, variable, label);
    }

    @Override
    public Instruction createNewInstructionWithNewLabel(Label newLabel) {
        return new DecreaseInstruction(getTargetVariable(), newLabel);
    }

    @Override
    public Label execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getTargetVariable());
        long newVariableValue = Math.max(variableValue - 1, 0);

        context.updateVariable(getTargetVariable() ,newVariableValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(variableRepresentation);
        command.append(" - 1");

        return command.toString();
    }

}
