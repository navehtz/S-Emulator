package engine.instruction;

import engine.execution.ExecutionContext;
import engine.label.Label;
import engine.label.FixedLabel;
import engine.variable.Variable;

public class DecreaseInstruction extends AbstractInstruction {

    public DecreaseInstruction(Variable variable) {
        super(InstructionData.DECREASE, variable, FixedLabel.EMPTY);
    }

    public DecreaseInstruction(Variable variable, Label label) {
        super(InstructionData.DECREASE, variable, label);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());

        variableValue = Math.max(--variableValue, 0);
        context.updateVariable(getVariable() ,variableValue);

        return FixedLabel.EMPTY;
    }


}
