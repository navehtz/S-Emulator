package instruction.synthetic;

import execution.ExecutionContext;
import instruction.*;
import instruction.synthetic.quoteArg.CallArg;
import instruction.synthetic.quoteArg.QuoteArg;
import label.FixedLabel;
import label.Label;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;

public class QuoteInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {

    private static final int MAX_DEGREE = 5;

    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final String functionName;
    private String displayName;
    private final List<QuoteArg> functionArguments;

    public QuoteInstruction(Variable targetVariable, Instruction origin, int instructionNumber, String functionName, List<QuoteArg> functionArguments) {
        super(InstructionData.QUOTE, InstructionType.SYNTHETIC ,targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.functionName = functionName;
        this.functionArguments = functionArguments != null ? functionArguments : List.of();
    }

    public QuoteInstruction(Variable targetVariable, Label label, Instruction origin, int instructionNumber, String functionName, List<QuoteArg> functionArguments) {
        super(InstructionData.QUOTE, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.functionName = functionName;
        this.functionArguments = functionArguments != null ? functionArguments : List.of();
    }

    public String getFunctionName() { return functionName; }

    public void setDisplayName(String displayName) {
        this.displayName = (displayName == null || displayName.isBlank())
                ? null : displayName;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        QuoteInstruction quoteInstruction = new QuoteInstruction(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber, functionName, copyArgs(functionArguments));

        quoteInstruction.setDisplayName(this.displayName);
        return quoteInstruction;
    }

    private static List<QuoteArg> copyArgs(List<QuoteArg> srcArguments) {
        List<QuoteArg> outArguments = new ArrayList<>(srcArguments.size());
        for (QuoteArg quoteArg : srcArguments) {
            if (quoteArg instanceof CallArg callArg) {
                CallArg copyOfCallArg = new CallArg(callArg.getCallName(), copyArgs(callArg.getArgs()));
                copyOfCallArg.setDisplayName(callArg.getDisplayName()); // add a getter or expose field
                outArguments.add(copyOfCallArg);
            } else { // VarArg
                outArguments.add(quoteArg); // immutable; safe to reuse
            }
        }
        return outArguments;
    }

    @Override
    public Label execute(ExecutionContext context) {
        if (functionName == null || functionName.isBlank()) {
            throw new IllegalArgumentException("QuoteInstruction: function name is missing");
        }

        long[] argsValues = new long[functionArguments.size()];

        for(int i = 0 ; i < functionArguments.size() ; i++) {
            argsValues[i] = functionArguments.get(i).eval(context);
        }

        long functionResult = context.invokeOperation(functionName, argsValues);

        context.updateVariable(getTargetVariable(), functionResult);
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String shownName = (displayName != null) ? displayName :functionName;
        String renderedArgs = functionArguments.stream()   // List<QuoteArg>
                .map(QuoteArg::render)
                .collect(java.util.stream.Collectors.joining(","));
        return getTargetVariable().getRepresentation()
                + " <- ("
                + shownName
                + (renderedArgs.isEmpty() ? "" : "," + renderedArgs)
                + ")";
    }

    @Override
    public Label getReferenceLabel() {
        return FixedLabel.EMPTY;
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
        innerInstructions.clear();
        innerInstructions.add(createInstructionWithInstructionNumber(startNumber));
        return startNumber + 1;
    }

    public List<QuoteArg> getFunctionArguments() { return functionArguments; }

}
