package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.*;
import engine.instruction.basic.DecreaseInstruction;
import engine.instruction.basic.JumpNotZeroInstruction;
import engine.instruction.basic.NoOpInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.label.LabelImpl;
import engine.variable.Variable;
import engine.variable.VariableImpl;
import engine.variable.VariableType;

import java.util.ArrayList;
import java.util.List;

public class JumpZeroInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {

    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Label referencesLabel;

    public JumpZeroInstruction(Variable variable, Label referencesLabel) {
        super(InstructionData.JUMP_ZERO, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY);
        this.referencesLabel = referencesLabel;
    }

    public JumpZeroInstruction(Variable variable, Label label, Label referencesLabel) {
        super(InstructionData.JUMP_ZERO, InstructionType.SYNTHETIC, variable, label);
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Instruction createNewInstructionWithNewLabel(Label newLabel) {
        return new JumpZeroInstruction(getTargetVariable(), newLabel, referencesLabel);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(this.getTargetVariable());

        return variableValue == 0 ? this.referencesLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(variableRepresentation);
        command.append(" = 0 GOTO ");
        command.append(referencesLabel.getLabelRepresentation());

        return command.toString();
    }

    @Override
    public Label getReferenceLabel() {
        return referencesLabel;
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public void setInnerInstructions() {
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        Label newLabel2 =  super.getProgramOfThisInstruction().generateUniqueLabel();

        innerInstructions.add(new JumpNotZeroInstruction(super.getTargetVariable(),newLabel1, newLabel2));
        innerInstructions.add(new GotoLabelInstruction(super.getTargetVariable(), referencesLabel)); // TODO: fix this, GOTO label shouldn't get any variable. see aviad github
        innerInstructions.add(new NoOpInstruction(Variable.RESULT, newLabel2));
    }
}
