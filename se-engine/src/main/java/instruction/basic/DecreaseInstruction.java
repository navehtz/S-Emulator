package instruction.basic;

import execution.ExecutionContext;
import instruction.*;
import label.Label;
import label.FixedLabel;
import operation.OperationView;
import variable.Variable;

import java.util.Map;

public class DecreaseInstruction extends AbstractInstruction {

    public DecreaseInstruction(Variable variable, Instruction origin, int instructionNumber) {
        super(InstructionData.DECREASE, InstructionType.BASIC, variable, FixedLabel.EMPTY, origin, instructionNumber);
    }

    public DecreaseInstruction(Variable variable, Label label, Instruction origin, int instructionNumber) {
        super(InstructionData.DECREASE, InstructionType.BASIC, variable, label, origin,  instructionNumber);
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new DecreaseInstruction(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {

        long variableValue = context.getVariableValue(getTargetVariable());
        long newVariableValue = Math.max(variableValue - 1, 0);

        context.updateVariable(getTargetVariable() ,newVariableValue);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(variableRepresentation);
        command.append(" - 1");

        return command.toString();
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> varMap, Map<Label, Label> labelMap, Instruction origin, OperationView mainProgram) {
        Variable tgt = RemapUtils.mapVar(varMap, getTargetVariable());
        Label lbl = RemapUtils.mapLbl(labelMap, getLabel());

        Instruction clonedInstruction = new DecreaseInstruction(tgt, lbl, origin, newInstructionNumber);
        clonedInstruction.setProgramOfThisInstruction(mainProgram);
        return clonedInstruction;
    }

}
