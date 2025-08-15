package engine.instruction;

import engine.label.Label;
import engine.label.FixedLabel;
import engine.variable.Variable;


public abstract class AbstractInstruction implements Instruction {

    private final InstructionData instructionData;
    private final Label label;
    private final Variable variable;

    public AbstractInstruction(InstructionData instructionData, Variable variable) {
        this(instructionData, variable,FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData instructionData, Variable variable, Label label) {
        this.instructionData = instructionData;
        this.variable = variable;
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
    public Variable getVariable() {
        return this.variable;
    }
}
