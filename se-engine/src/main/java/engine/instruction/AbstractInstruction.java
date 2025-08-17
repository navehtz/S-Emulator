package engine.instruction;

import engine.label.Label;
import engine.label.FixedLabel;
import engine.variable.Variable;


public abstract class AbstractInstruction implements Instruction {

    private final InstructionData instructionData;
    private final InstructionType instructionType;
    private final Label label;
    private final Variable targetVariable;

    public AbstractInstruction(InstructionData instructionData, InstructionType instructionType, Variable targetVariable) {
        this(instructionData, instructionType, targetVariable,FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData instructionData, InstructionType instructionType, Variable targetVariable, Label label) {
        this.instructionData = instructionData;
        this.instructionType = instructionType;
        this.targetVariable = targetVariable;
        this.label = label;
    }

    @Override
    public String getName() {
        return this.instructionData.getName();
    }

    @Override
    public int getCycles() {
        return this.instructionData.getCycles();
    }

    @Override
    public Label getLabel() {
        return this.label;
    }

    @Override
    public Variable getTargetVariable() {
        return this.targetVariable;
    }

    @Override
    public Variable getSourceVariable() {
        return null;
    }

    @Override
    public String instructionRepresentation(int InstructionNumber) {
        StringBuilder instructionDisplay = new StringBuilder();
        String labelPadded = labelPadding(getLabel().getLabelRepresentation());
        instructionDisplay.append("#").append(InstructionNumber);
        instructionDisplay.append(" (").append(instructionType.getInstructionType()).append(")");
        instructionDisplay.append("[ ").append(labelPadded).append("] ");
        instructionDisplay.append(this.getCommand());
        instructionDisplay.append(" (").append(getCycles()).append(")");

        return instructionDisplay.toString();
    }

    private String labelPadding(String labelStr) {
            return String.format("%-4s", labelStr);
    }

}