package engine.instruction;

import engine.label.Label;
import engine.label.FixedLabel;
import engine.variable.Variable;

import java.util.List;


public abstract class AbstractInstruction implements Instruction {

    private final InstructionData instructionData;
    private final InstructionType instructionType;
    private final Label label;
    private final Variable variable;

    public AbstractInstruction(InstructionData instructionData, InstructionType instructionType, Variable variable) {
        this(instructionData, instructionType, variable,FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData instructionData, InstructionType instructionType, Variable variable, Label label) {
        this.instructionData = instructionData;
        this.instructionType = instructionType;
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

    @Override
    public void printInstruction(int InstructionNumber) {


        StringBuilder instructionDisplay = new StringBuilder();
        instructionDisplay.append("#").append(InstructionNumber);
        instructionDisplay.append("(").append(instructionType.getInstructionType()).append(")");
        instructionDisplay.append("[").append(label.getLabelRepresentation()).append("]");
        instructionDisplay.append(this.getCommand());
        instructionDisplay.append("(").append(getCycles()).append(")");

        System.out.println(instructionDisplay.toString());
    }
}

// לכתוב את COMMAND
// לתקן את הקריאה לפונקציה - היא מקבלת פרמטר,
// להחזיר רשימה ובסוף הפונקציה הראשית להדפיס
// אולי לא, אולי כדאי ישר להדפיס
