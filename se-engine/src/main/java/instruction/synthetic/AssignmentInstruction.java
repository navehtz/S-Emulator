package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.basic.DecreaseInstruction;
import instruction.basic.IncreaseInstruction;
import instruction.basic.JumpNotZeroInstruction;
import instruction.basic.NoOpInstruction;
import label.FixedLabel;
import label.Label;
import operation.Operation;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssignmentInstruction extends AbstractInstruction implements SyntheticInstruction, SourceVariableInstruction {
    private final int MAX_DEGREE = 2;
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Variable sourceVariable;

    public AssignmentInstruction(Variable targetVariable, Variable sourceVariable, Instruction origin, int instructionNumber) {
        super(InstructionData.ASSIGNMENT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.sourceVariable = sourceVariable;
    }

    public AssignmentInstruction(Variable targetVariable, Label label, Variable sourceVariable, Instruction origin, int instructionNumber) {
        super(InstructionData.ASSIGNMENT, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.sourceVariable = sourceVariable;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new AssignmentInstruction(getTargetVariable(), getLabel(), sourceVariable, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long sourceVariableValue = context.getVariableValue(sourceVariable);
        context.updateVariable(getTargetVariable(), sourceVariableValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        String sourceVariableRepresentation = sourceVariable.getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(targetVariableRepresentation);
        command.append(" <- ");
        command.append(sourceVariableRepresentation);

        return command.toString();
    }

    @Override
    public Variable getSourceVariable() {
        return sourceVariable;
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
        Label newLabel2 =  super.getProgramOfThisInstruction().generateUniqueLabel();
        Label newLabel3 =  super.getProgramOfThisInstruction().generateUniqueLabel();
        Label newLabel4 =  super.getProgramOfThisInstruction().generateUniqueLabel();
        int instructionNumber = startNumber;

        innerInstructions.add(new ZeroVariableInstruction(super.getTargetVariable(), newLabel1,this, instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(sourceVariable, newLabel2, this, instructionNumber++));
        innerInstructions.add(new GotoLabelInstruction(workVariable1, newLabel4, this, instructionNumber++));
        innerInstructions.add(new DecreaseInstruction(sourceVariable, newLabel2, this, instructionNumber++));
        innerInstructions.add(new IncreaseInstruction(workVariable1, this, instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(sourceVariable, newLabel2, this, instructionNumber++));

        innerInstructions.add(new DecreaseInstruction(workVariable1, newLabel3, this, instructionNumber++));
        innerInstructions.add(new IncreaseInstruction(super.getTargetVariable(), this, instructionNumber++));
        innerInstructions.add(new IncreaseInstruction(sourceVariable, this, instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(workVariable1, newLabel3, this, instructionNumber++));

        innerInstructions.add(new NoOpInstruction(super.getTargetVariable(), newLabel4, this, instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> varMap, Map<Label, Label> labelMap, Instruction origin, Operation mainProgram) {
        Variable tgtLbl = RemapUtils.mapVar(varMap, getTargetVariable());
        Label newLbl = RemapUtils.mapLbl(labelMap, getLabel());
        Variable newSrcVar = RemapUtils.mapVar(varMap, getSourceVariable());

        Instruction clonedInstruction = new AssignmentInstruction(tgtLbl, newLbl, newSrcVar, origin, newInstructionNumber);
        clonedInstruction.setProgramOfThisInstruction(mainProgram);
        return clonedInstruction;
    }
}
