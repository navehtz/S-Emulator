package engine.instruction;

import engine.execution.ExecutionContext;
import engine.label.Label;
import engine.label.FixedLabel;
import engine.variable.Variable;

public class IncreaseInstruction extends AbstractInstruction {

    public IncreaseInstruction(Variable variable) {
        super(InstructionData.INCREASE, variable, FixedLabel.EMPTY);
    }

    public IncreaseInstruction(Variable variable, Label label) {
        super(InstructionData.INCREASE, variable, label);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getVariable());

        variableValue++;
        context.updateVariable(getVariable() ,variableValue);

        return FixedLabel.EMPTY;
    }
}
