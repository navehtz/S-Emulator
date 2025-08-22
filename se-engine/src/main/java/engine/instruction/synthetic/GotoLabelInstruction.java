package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.*;
import engine.instruction.basic.DecreaseInstruction;
import engine.instruction.basic.IncreaseInstruction;
import engine.instruction.basic.JumpNotZeroInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.label.LabelImpl;
import engine.variable.Variable;
import engine.variable.VariableImpl;
import engine.variable.VariableType;

import java.util.ArrayList;
import java.util.List;

public class GotoLabelInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {

    private final List<Instruction> innerInstructions = new ArrayList<>();;
    private final Label referencesLabel;

    public GotoLabelInstruction(Variable variable, Label referencesLabel) {
        super(InstructionData.GOTO_LABEL, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY);
        this.referencesLabel = referencesLabel;
    }

    public GotoLabelInstruction(Variable variable, Label label, Label referencesLabel) {
        super(InstructionData.GOTO_LABEL, InstructionType.SYNTHETIC, variable, label);
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Instruction createNewInstructionWithNewLabel(Label newLabel) {
        return new GotoLabelInstruction(getTargetVariable(), newLabel, referencesLabel);
    }

    @Override
    public Label execute(ExecutionContext context) {
        return referencesLabel;
    }

    @Override
    public String getCommand() {
        String labelRepresentation = referencesLabel.getLabelRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("GOTO ");
        command.append(labelRepresentation);

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
        Variable workVariable1 = super.getProgramOfThisInstruction().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();


        innerInstructions.add(new IncreaseInstruction(workVariable1, newLabel1));
        innerInstructions.add(new JumpNotZeroInstruction(workVariable1, this.referencesLabel));
    }
}
