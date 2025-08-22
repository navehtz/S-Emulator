package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.*;
import engine.instruction.basic.IncreaseInstruction;
import engine.instruction.basic.JumpNotZeroInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;
import engine.variable.VariableImpl;
import engine.variable.VariableType;

import java.util.ArrayList;
import java.util.List;

public class ConstantAssignmentInstruction extends AbstractInstruction implements SyntheticInstruction {

    private final List<Instruction> innerInstructions = new ArrayList<>();
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
    public Instruction createNewInstructionWithNewLabel(Label newLabel) {
        return new ConstantAssignmentInstruction(getTargetVariable(), newLabel, constantValue);
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

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public void setInnerInstructions() {
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();

        innerInstructions.add(new ZeroVariableInstruction(super.getTargetVariable(), newLabel1));

        for(int i = 0 ; i < constantValue ; i++) {
            innerInstructions.add(new IncreaseInstruction(super.getTargetVariable()));
        }
    }
}
