package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.basic.DecreaseInstruction;
import instruction.basic.JumpNotZeroInstruction;
import instruction.basic.NoOpInstruction;
import label.FixedLabel;
import label.Label;
import operation.Operation;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualConstantInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {
    private final int MAX_DEGREE = 3;
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Label referencelabel;
    private final long constantValue;

    public JumpEqualConstantInstruction(Variable targetVariable, long constantValue, Label referencelabel, Instruction origin, int instructionNumber) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.constantValue = constantValue;
        this.referencelabel = referencelabel;

    }

    public JumpEqualConstantInstruction(Variable targetVariable, Label label, long constantValue, Label referencelabel, Instruction origin, int instructionNumber) {
        super(InstructionData.JUMP_EQUAL_CONSTANT, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.constantValue = constantValue;
        this.referencelabel = referencelabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new JumpEqualConstantInstruction(getTargetVariable(), getLabel(), constantValue, referencelabel, getOriginalInstruction(), instructionNumber);
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
    public int getMaxDegree() {
        return MAX_DEGREE;
    }

    @Override
    public int expandInstruction(int startNumber) {
        Variable workVariable1 = super.getProgramOfThisInstruction().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        Label newLabel2 = super.getProgramOfThisInstruction().generateUniqueLabel();
        int instructionNumber = startNumber;

        innerInstructions.add(new AssignmentInstruction(workVariable1, newLabel1 ,super.getTargetVariable(), this, instructionNumber++));

        for(int i = 0 ; i < constantValue ; i++) {
            innerInstructions.add(new JumpZeroInstruction(workVariable1, newLabel2, this, instructionNumber++));
            innerInstructions.add(new DecreaseInstruction(workVariable1, this, instructionNumber++));
        }

        innerInstructions.add(new JumpNotZeroInstruction(workVariable1, newLabel2, this, instructionNumber++));
        innerInstructions.add(new GotoLabelInstruction(super.getTargetVariable(), referencelabel, this, instructionNumber++));
        innerInstructions.add(new NoOpInstruction(Variable.RESULT, newLabel2, this, instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> varMap, Map<Label, Label> labelMap, Instruction origin, Operation mainProgram) {
        Variable tgtLbl = RemapUtils.mapVar(varMap, getTargetVariable());
        Label newLbl = RemapUtils.mapLbl(labelMap, getLabel());
        Label newRefLbl = RemapUtils.mapLbl(labelMap, getReferenceLabel());

        Instruction clonedInstruction = new JumpEqualConstantInstruction(tgtLbl, newLbl, this.constantValue, newRefLbl, origin, newInstructionNumber);
        clonedInstruction.setProgramOfThisInstruction(mainProgram);
        return clonedInstruction;
    }
}
