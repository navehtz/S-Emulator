package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.basic.DecreaseInstruction;
import instruction.basic.NoOpInstruction;
import label.FixedLabel;
import label.Label;
import operation.OperationView;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualVariableInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction, SourceVariableInstruction {
    private final int MAX_DEGREE = 3;
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Label referencesLabel;
    private final Variable sourceVariable;

    public JumpEqualVariableInstruction(Variable targetVariable, Variable sourceVariable, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.sourceVariable = sourceVariable;
        this.referencesLabel = referencesLabel;
    }

    public JumpEqualVariableInstruction(Variable targetVariable, Label label, Variable sourceVariable, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(InstructionData.JUMP_EQUAL_VARIABLE, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.sourceVariable = sourceVariable;
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new JumpEqualVariableInstruction(getTargetVariable(), getLabel(), sourceVariable, referencesLabel, getOriginalInstruction(), instructionNumber);
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
    public int getMaxDegree() {
        return MAX_DEGREE;
    }

    @Override
    public int expandInstruction(int startNumber) {
        int instructionNumber = startNumber;
        Variable workVariable1 = super.getProgramOfThisInstruction().generateUniqueVariable();
        Variable workVariable2 = super.getProgramOfThisInstruction().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        Label newLabel2 = super.getProgramOfThisInstruction().generateUniqueLabel();
        Label newLabel3 = super.getProgramOfThisInstruction().generateUniqueLabel();
        Label newLabel4 = super.getProgramOfThisInstruction().generateUniqueLabel();

        innerInstructions.add(new AssignmentInstruction(workVariable1, newLabel1, super.getTargetVariable(), this, instructionNumber++));
        innerInstructions.add(new AssignmentInstruction(workVariable2,sourceVariable, this, instructionNumber++));

        innerInstructions.add(new JumpZeroInstruction(workVariable1, newLabel3, newLabel4, this, instructionNumber++));
        innerInstructions.add(new JumpZeroInstruction(workVariable2, newLabel2, this, instructionNumber++));
        innerInstructions.add(new DecreaseInstruction(workVariable1, this, instructionNumber++));
        innerInstructions.add(new DecreaseInstruction(workVariable2, this, instructionNumber++));
        innerInstructions.add(new GotoLabelInstruction(workVariable1, newLabel3, this, instructionNumber++));
        innerInstructions.add(new JumpZeroInstruction(workVariable2, newLabel4, referencesLabel, this, instructionNumber++));
        innerInstructions.add(new NoOpInstruction(Variable.RESULT, newLabel2, this, instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> varMap, Map<Label, Label> labelMap, Instruction origin, OperationView mainProgram) {
        Variable tgtLbl = RemapUtils.mapVar(varMap, getTargetVariable());
        Label referenceLabel = RemapUtils.mapLbl(labelMap, getReferenceLabel());
        Variable newSrcVar = RemapUtils.mapVar(varMap, getSourceVariable());
        Label newLbl = RemapUtils.mapLbl(labelMap, getLabel());

        Instruction clonedInstruction = new JumpEqualVariableInstruction(tgtLbl, newLbl, newSrcVar, referenceLabel, origin, newInstructionNumber);
        clonedInstruction.setProgramOfThisInstruction(mainProgram);
        return clonedInstruction;
    }
}
