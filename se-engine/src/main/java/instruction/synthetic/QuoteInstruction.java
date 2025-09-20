package instruction.synthetic;

import execution.ExecutionContext;
import function.Function;
import function.FunctionImpl;
import instruction.*;
import instruction.basic.DecreaseInstruction;
import instruction.basic.NoOpInstruction;
import label.FixedLabel;
import label.Label;
import operation.Operation;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class QuoteInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {
    private final int MAX_DEGREE = 5;
    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Label referencesLabel;
    private final Operation function;

    public QuoteInstruction(Variable targetVariable, Operation function, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(InstructionData.QUOTE, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.function = function;
        this.referencesLabel = referencesLabel;
    }

    public QuoteInstruction(Variable targetVariable, Label label, Operation function, Label referencesLabel, Instruction origin, int instructionNumber) {
        super(InstructionData.QUOTE, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.function = function;
        this.referencesLabel = referencesLabel;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        return new QuoteInstruction(getTargetVariable(), getLabel(), function, referencesLabel, getOriginalInstruction(), instructionNumber);
    }

    @Override
    public Label execute(ExecutionContext context) {
        long functionResult = context.getOperationResult(function);
        context.updateVariable(getTargetVariable(), functionResult);

        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String targetVariableRepresentation = getTargetVariable().getRepresentation();
        StringBuilder command = new StringBuilder();
        command.append(targetVariableRepresentation);
        command.append(" <- ");
        command.append("(");
        command.append(function.getName());
        command.append(",");
        command.append(String.join(",", function.getInputVariables().stream().map(Variable::getRepresentation).toArray(String[]::new) ) );
        command.append(")");

        return command.toString();
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
    public int setInnerInstructionsAndReturnTheNextOne(int startNumber) {
        int instructionNumber = startNumber;




//        Variable workVariable1 = super.getProgramOfThisInstruction().generateUniqueVariable();
//        Variable workVariable2 = super.getProgramOfThisInstruction().generateUniqueVariable();
//        Label newLabel1 = (super.getLabel() == FixedLabel.EMPTY) ? FixedLabel.EMPTY : super.getLabel();
//        Label newLabel2 = super.getProgramOfThisInstruction().generateUniqueLabel();
//        Label newLabel3 = super.getProgramOfThisInstruction().generateUniqueLabel();
//        Label newLabel4 = super.getProgramOfThisInstruction().generateUniqueLabel();
//
//        innerInstructions.add(new AssignmentInstruction(workVariable1, newLabel1, super.getTargetVariable(), this, instructionNumber++));
//        innerInstructions.add(new AssignmentInstruction(workVariable2,sourceVariable, this, instructionNumber++));
//
//        innerInstructions.add(new JumpZeroInstruction(workVariable1, newLabel3, newLabel4, this, instructionNumber++));
//        innerInstructions.add(new JumpZeroInstruction(workVariable2, newLabel2, this, instructionNumber++));
//        innerInstructions.add(new DecreaseInstruction(workVariable1, this, instructionNumber++));
//        innerInstructions.add(new DecreaseInstruction(workVariable2, this, instructionNumber++));
//        innerInstructions.add(new GotoLabelInstruction(workVariable1, newLabel3, this, instructionNumber++));
//        innerInstructions.add(new JumpZeroInstruction(workVariable2, newLabel4, referencesLabel, this, instructionNumber++));
//        innerInstructions.add(new NoOpInstruction(Variable.RESULT, newLabel2, this, instructionNumber++));

        return instructionNumber;
    }
}
