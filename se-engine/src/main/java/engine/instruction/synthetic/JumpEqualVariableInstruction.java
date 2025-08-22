package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.*;
import engine.instruction.basic.DecreaseInstruction;
import engine.instruction.basic.NoOpInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;


import java.util.ArrayList;
import java.util.List;

public class JumpEqualVariableInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {

    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Label referencesLabel;
    private final Variable sourceVariable;

    public JumpEqualVariableInstruction(Variable targetVariable, Variable sourceVariable, Label referencesLabel) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY);
        this.sourceVariable = sourceVariable;
        this.referencesLabel = referencesLabel;
    }

    public JumpEqualVariableInstruction(Variable targetVariable, Label label, Variable sourceVariable, Label referencesLabel) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, InstructionType.SYNTHETIC, targetVariable, label);
        this.sourceVariable = sourceVariable;
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Instruction createNewInstructionWithNewLabel(Label newLabel) {
        return new JumpEqualVariableInstruction(getTargetVariable(), newLabel, sourceVariable, referencesLabel);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long targetVariableValue = context.getVariableValue(getTargetVariable());
        long sourceVariableValue = context.getVariableValue(sourceVariable);

        return (targetVariableValue == sourceVariableValue) ? referencesLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String sourceVariableRepresentation = sourceVariable.getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(targetVariableRepresentation);
        command.append(" = ");
        command.append(sourceVariableRepresentation);
        command.append(" GOTO ");
        command.append(referencesLabel.getLabelRepresentation());

        return command.toString();
    }

    @Override
    public Variable getSourceVariable() {
        return sourceVariable;
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
        Variable workVariable1 = super.getProgramOfThisInstruction().generateUniqueVariable();
        Variable workVariable2 = super.getProgramOfThisInstruction().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        Label newLabel2 = super.getProgramOfThisInstruction().generateUniqueLabel();
        Label newLabel3 = super.getProgramOfThisInstruction().generateUniqueLabel();
        Label newLabel4 = super.getProgramOfThisInstruction().generateUniqueLabel();

        innerInstructions.add(new AssignmentInstruction(workVariable1, newLabel1, super.getTargetVariable()));
        innerInstructions.add(new AssignmentInstruction(workVariable2,sourceVariable));

        innerInstructions.add(new JumpZeroInstruction(workVariable1, newLabel3, newLabel4));
        innerInstructions.add(new JumpZeroInstruction(workVariable2, newLabel2));
        innerInstructions.add(new DecreaseInstruction(workVariable1));
        innerInstructions.add(new DecreaseInstruction(workVariable2));
        innerInstructions.add(new GotoLabelInstruction(workVariable1, newLabel3)); // TODO: fix this, GOTO label shouldn't get any variable. see aviad github
        innerInstructions.add(new JumpZeroInstruction(workVariable2, newLabel4, referencesLabel));
        innerInstructions.add(new NoOpInstruction(Variable.RESULT, newLabel2));
    }
}
