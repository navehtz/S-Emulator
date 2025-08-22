package engine.instruction.synthetic;

import engine.execution.ExecutionContext;
import engine.instruction.*;
import engine.instruction.basic.DecreaseInstruction;
import engine.instruction.basic.IncreaseInstruction;
import engine.instruction.basic.JumpNotZeroInstruction;
import engine.instruction.basic.NoOpInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class AssignmentInstruction extends AbstractInstruction implements SyntheticInstruction {

    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Variable sourceVariable;

    public AssignmentInstruction(Variable targetVariable, Variable sourceVariable) {
        super(InstructionData.ASSIGNMENT, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY);
        this.sourceVariable = sourceVariable;
    }

    public AssignmentInstruction(Variable targetVariable, Label label, Variable sourceVariable) {
        super(InstructionData.ASSIGNMENT, InstructionType.SYNTHETIC, targetVariable, label);
        this.sourceVariable = sourceVariable;
    }

    @Override
    public Instruction createNewInstructionWithNewLabel(Label newLabel) {
        return new AssignmentInstruction(getTargetVariable(), newLabel, sourceVariable);
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
    public void setInnerInstructions() {
        Variable workVariable1 = super.getProgramOfThisInstruction().generateUniqueVariable();
        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
        Label newLabel2 =  super.getProgramOfThisInstruction().generateUniqueLabel();
        Label newLabel3 =  super.getProgramOfThisInstruction().generateUniqueLabel();
        Label newLabel4 =  super.getProgramOfThisInstruction().generateUniqueLabel();

        innerInstructions.add(new ZeroVariableInstruction(super.getTargetVariable(), newLabel1));
        innerInstructions.add(new JumpNotZeroInstruction(sourceVariable, newLabel2));
        innerInstructions.add(new GotoLabelInstruction(workVariable1, newLabel4)); // TODO: fix this, GOTO label shouldn't get any variable. see aviad github
        innerInstructions.add(new DecreaseInstruction(sourceVariable, newLabel2));
        innerInstructions.add(new IncreaseInstruction(workVariable1));
        innerInstructions.add(new JumpNotZeroInstruction(sourceVariable, newLabel2));

        innerInstructions.add(new DecreaseInstruction(workVariable1, newLabel3));
        innerInstructions.add(new IncreaseInstruction(super.getTargetVariable()));
        innerInstructions.add(new IncreaseInstruction(sourceVariable));
        innerInstructions.add(new JumpNotZeroInstruction(workVariable1, newLabel3));

        innerInstructions.add(new NoOpInstruction(super.getTargetVariable(), newLabel4));
    }
}
