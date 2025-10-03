package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.basic.DecreaseInstruction;
import instruction.basic.JumpNotZeroInstruction;
import label.FixedLabel;
import label.Label;
import operation.OperationView;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZeroVariableInstruction extends AbstractInstruction implements SyntheticInstruction {
    private final int MAX_DEGREE = 1;
    private final List<Instruction> innerInstructions = new ArrayList<>();;

    public ZeroVariableInstruction(Variable variable, Instruction origin, int instructionNumber) {
        super(InstructionData.ZERO_VARIABLE, InstructionType.SYNTHETIC ,variable, FixedLabel.EMPTY, origin, instructionNumber);
    }

    public ZeroVariableInstruction(Variable variable, Label label, Instruction origin, int instructionNumber) {
        super(InstructionData.ZERO_VARIABLE, InstructionType.SYNTHETIC, variable, label, origin, instructionNumber);
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new ZeroVariableInstruction(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        context.updateVariable(getTargetVariable() ,0);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(0);

        return command.toString();
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
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? super.getProgramOfThisInstruction().generateUniqueLabel() : super.getLabel();

        innerInstructions.add(new DecreaseInstruction(super.getTargetVariable(), newLabel1, this,  instructionNumber++));
        innerInstructions.add(new JumpNotZeroInstruction(super.getTargetVariable(), newLabel1, this, instructionNumber++));

        return instructionNumber;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> varMap, Map<Label, Label> labelMap, Instruction origin, OperationView mainProgram) {
        Variable tgtLbl = RemapUtils.mapVar(varMap, getTargetVariable());
        Label newLbl = RemapUtils.mapLbl(labelMap, getLabel());

        Instruction clonedInstruction = new ZeroVariableInstruction(tgtLbl, newLbl, origin, newInstructionNumber);
        clonedInstruction.setProgramOfThisInstruction(mainProgram);
        return clonedInstruction;
    }
}
