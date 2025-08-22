package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.*;
import engine.instruction.basic.DecreaseInstruction;
import engine.instruction.basic.JumpNotZeroInstruction;
import engine.instruction.basic.NoOpInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class JumpEqualConstantInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {

    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Label referencelabel;
    private final long constantValue;

    public JumpEqualConstantInstruction(Variable targetVariable, long constantValue, Label referencelabel) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY);
        this.constantValue = constantValue;
        this.referencelabel = referencelabel;

    }

    public JumpEqualConstantInstruction(Variable targetVariable, Label label, long constantValue, Label referencelabel) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, InstructionType.SYNTHETIC, targetVariable, label);
        this.constantValue = constantValue;
        this.referencelabel = referencelabel;
    }

    @Override
    public Instruction createNewInstructionWithNewLabel(Label newLabel) {
        return new JumpEqualConstantInstruction(getTargetVariable(), newLabel, constantValue, referencelabel);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(getTargetVariable());

        return (variableValue == constantValue) ? referencelabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(variableRepresentation);
        command.append(" = ");
        command.append(constantValue);
        command.append(" GOTO ");
        command.append(referencelabel.getLabelRepresentation());

        return command.toString();
    }

    @Override
    public Label getReferenceLabel() {
        return referencelabel;
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public void setInnerInstructions() {
        Variable workVariable1 = super.getProgramOfThisInstruction().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        Label newLabel2 = super.getProgramOfThisInstruction().generateUniqueLabel();

        innerInstructions.add(new AssignmentInstruction(workVariable1, newLabel1 ,super.getTargetVariable()));

        for(int i = 0 ; i < constantValue ; i++) {
            innerInstructions.add(new JumpZeroInstruction(workVariable1, newLabel2));
            innerInstructions.add(new DecreaseInstruction(workVariable1));
        }

        innerInstructions.add(new JumpNotZeroInstruction(workVariable1, newLabel2));
        innerInstructions.add(new GotoLabelInstruction(super.getTargetVariable(), referencelabel)); // TODO: fix this, GOTO label shouldn't get any variable. see aviad github
        innerInstructions.add(new NoOpInstruction(Variable.RESULT, newLabel2));
    }
}
