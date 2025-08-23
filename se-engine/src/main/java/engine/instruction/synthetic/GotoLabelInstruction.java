package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.*;
import engine.instruction.basic.DecreaseInstruction;
import engine.instruction.basic.IncreaseInstruction;
import engine.instruction.basic.JumpNotZeroInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class GotoLabelInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {

    private final List<Instruction> innerInstructions = new ArrayList<>();;
    private final Label referencesLabel;

    public GotoLabelInstruction(Variable variable, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(InstructionData.GOTO_LABEL, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY, origin, instructionNumber);
        this.referencesLabel = referencesLabel;
    }

    public GotoLabelInstruction(Variable variable, Label label, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(InstructionData.GOTO_LABEL, InstructionType.SYNTHETIC, variable, label, origin, instructionNumber);
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new GotoLabelInstruction(getTargetVariable(), getLabel(), referencesLabel, getOriginalInstruction(), instructionNumber);
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
    public int getMaxDegree() {
        int maxDegree = 1;
        return maxDegree;
    }

    @Override
    public int setInnerInstructionsAndReturnTheNextOne(int startNumber) {
        Variable workVariable1 = super.getProgramOfThisInstruction().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        int instructionNumber = startNumber;

        innerInstructions.add(new IncreaseInstruction(workVariable1, newLabel1, this, instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(workVariable1, this.referencesLabel, this,  instructionNumber++));

        return instructionNumber;
    }

}
