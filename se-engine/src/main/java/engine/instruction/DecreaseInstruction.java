package engine.instruction;

import engine.execution.ExecutionContext;
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
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());
        long newVariableValue = Math.max(variableValue - 1, 0);

        context.updateVariable(getVariable() ,newVariableValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getVariable().getRepresentation();

        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append("<-");
        command.append(variableRepresentation);
        command.append("+1");

        return command.toString();
    }

}
