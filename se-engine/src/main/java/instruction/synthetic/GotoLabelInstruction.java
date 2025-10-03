package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.basic.IncreaseInstruction;
import instruction.basic.JumpNotZeroInstruction;
import label.FixedLabel;
import label.Label;
import operation.Operation;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GotoLabelInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {
    private final int MAX_DEGREE = 1;
    private final List<Instruction> innerInstructions = new ArrayList<>();
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
        return referencesLabel == null ? FixedLabel.EMPTY : referencesLabel;
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public int getMaxDegree() {
        return MAX_DEGREE;
    }

    @Override
    public int expandInstruction(int startNumber) {
        Variable workVariable1 = super.getProgramOfThisInstruction().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        int instructionNumber = startNumber;

        innerInstructions.add(new IncreaseInstruction(workVariable1, newLabel1, this, instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(workVariable1, this.referencesLabel, this,  instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> varMap, Map<Label, Label> labelMap, Instruction origin, Operation mainProgram) {
        Variable tgtLbl = RemapUtils.mapVar(varMap, getTargetVariable());
        Label newLbl = RemapUtils.mapLbl(labelMap, getLabel());
        Label newRefLbl = RemapUtils.mapLbl(labelMap, getReferenceLabel());

        Instruction clonedInstruction = new GotoLabelInstruction(tgtLbl, newLbl, newRefLbl, origin, newInstructionNumber);
        clonedInstruction.setProgramOfThisInstruction(mainProgram);
        return clonedInstruction;
    }
}
