package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.*;
import engine.instruction.basic.DecreaseInstruction;
import engine.instruction.basic.JumpNotZeroInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class ZeroVariableInstruction extends AbstractInstruction implements SyntheticInstruction {

    private final List<Instruction> innerInstructions = new ArrayList<>();;

    public ZeroVariableInstruction(Variable variable) {
        super(InstructionData.ZERO_VARIABLE, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY);
    }

    public ZeroVariableInstruction(Variable variable, Label label) {
        super(InstructionData.ZERO_VARIABLE, InstructionType.SYNTHETIC, variable, label);
    }

    @Override
    public Instruction createNewInstructionWithNewLabel(Label newLabel) {
        return new ZeroVariableInstruction(getTargetVariable(), newLabel);
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getTargetVariable() ,0);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(0);

        return command.toString();
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public void setInnerInstructions() {
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? super.getProgramOfThisInstruction().generateUniqueLabel() : super.getLabel();

        innerInstructions.add(new DecreaseInstruction(super.getTargetVariable(), newLabel1));
        innerInstructions.add(new JumpNotZeroInstruction(super.getTargetVariable(), newLabel1));
    }
}
