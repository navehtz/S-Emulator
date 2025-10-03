package instruction.basic;

import execution.ExecutionContext;
import label.FixedLabel;
import label.Label;
import operation.Operation;
import variable.Variable;
import instruction.*;

import java.util.Map;

public class JumpNotZeroInstruction extends AbstractInstruction implements LabelReferencesInstruction {
    private final Label referencesLabel;

    public JumpNotZeroInstruction(Variable variable, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(InstructionData.JUMP_NOT_ZERO, InstructionType.BASIC ,variable, FixedLabel.EMPTY, origin, instructionNumber);
        this.referencesLabel = referencesLabel;
    }

    public JumpNotZeroInstruction(Variable variable, Label label, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(InstructionData.JUMP_NOT_ZERO, InstructionType.BASIC, variable, label,  origin, instructionNumber);
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new JumpNotZeroInstruction(getTargetVariable(), getLabel(), referencesLabel, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long variableValue = context.getVariableValue(this.getTargetVariable());

        return variableValue != 0 ? this.referencesLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append("IF ");
        command.append(variableRepresentation);
        command.append(" != 0 GOTO ");
        command.append(referencesLabel.getLabelRepresentation());

        return command.toString();
    }

    @Override
    public Label getReferenceLabel() {
        return referencesLabel == null ? FixedLabel.EMPTY : referencesLabel;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> varMap, Map<Label, Label> labelMap, Instruction origin, Operation mainProgram) {
        Variable tgtLbl = RemapUtils.mapVar(varMap, getTargetVariable());
        Label newLbl = RemapUtils.mapLbl(labelMap, getLabel());
        Label refLbl = RemapUtils.mapLbl(labelMap, getReferenceLabel());

        Instruction clonedInstruction = new JumpNotZeroInstruction(tgtLbl, newLbl, refLbl, origin, newInstructionNumber);
        clonedInstruction.setProgramOfThisInstruction(mainProgram);
        return clonedInstruction;
    }
}
