package engine.instruction.basic;

import engine.execution.ExecutionContext;
import engine.instruction.AbstractInstruction;
import engine.instruction.Instruction;
import engine.instruction.InstructionData;
import engine.instruction.InstructionType;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

public class NoOpInstruction extends AbstractInstruction {

    public NoOpInstruction(Variable variable, Instruction origin, int instructionNumber) {
        super(InstructionData.NO_OP, InstructionType.BASIC ,variable, FixedLabel.EMPTY, origin ,instructionNumber);
    }

    public NoOpInstruction(Variable variable, Label label, Instruction origin, int instructionNumber) {
        super(InstructionData.NO_OP, InstructionType.BASIC, variable, label,  origin, instructionNumber);
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new NoOpInstruction(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String variableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();

        command.append(variableRepresentation);
        command.append(" <- ");
        command.append(variableRepresentation);

        return command.toString();
    }
}
