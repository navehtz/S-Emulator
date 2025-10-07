package instruction.synthetic;

import execution.ExecutionContext;
import instruction.AbstractInstruction;
import instruction.Instruction;
import instruction.InstructionData;
import instruction.InstructionType;
import instruction.LabelReferencesInstruction;
import instruction.RemapUtils;
import instruction.SyntheticInstruction;
import instruction.synthetic.quoteArg.CallArg;
import instruction.synthetic.quoteArg.QuoteArg;
import instruction.synthetic.quoteArg.VarArg;
import label.FixedLabel;
import label.Label;
import operation.OperationView;
import variable.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JumpEqualFunctionInstruction extends AbstractInstruction
        implements SyntheticInstruction, LabelReferencesInstruction {

    private static final int MAX_DEGREE = 6; // expands to QUOTE + JUMP_EQUAL_VARIABLE

    private final String functionName;
    private final List<QuoteArg> functionArguments = new ArrayList<>();
    private final Label jumpTargetLabel;

    private String displayName; // optional pretty name for UI only
    private int currentCyclesNumber;

    private final List<Instruction> innerInstructions = new ArrayList<>();

    public JumpEqualFunctionInstruction(Variable targetVariable,
                                        Label label,
                                        Label jumpTargetLabel,
                                        Instruction origin,
                                        int instructionNumber,
                                        String functionName,
                                        List<QuoteArg> functionArguments) {
        super(InstructionData.JUMP_EQUAL_FUNCTION, InstructionType.SYNTHETIC,
                targetVariable, label, origin, instructionNumber);
        this.functionName = Objects.requireNonNull(functionName, "functionName");
        this.jumpTargetLabel = Objects.requireNonNull(jumpTargetLabel, "jumpTargetLabel");
        if (functionArguments != null) this.functionArguments.addAll(functionArguments);
    }

    // ---------- UI helpers ----------
    public void setDisplayName(String displayName) {
        this.displayName = (displayName == null || displayName.isBlank()) ? null : displayName;
    }

    public String getFunctionName() { return functionName; }
    public List<QuoteArg> getFunctionArguments() { return functionArguments; }

    // ---------- Instruction API ----------
    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        JumpEqualFunctionInstruction copy = new JumpEqualFunctionInstruction(
                getTargetVariable(),
                getLabel(),
                jumpTargetLabel,
                getOriginalInstruction(),
                instructionNumber,
                this.functionName,
                deepCopyArgs(this.functionArguments)
        );
        copy.setDisplayName(this.displayName);
        return copy;
    }

    @Override
    public Label execute(ExecutionContext context) {
        long leftValue = context.getVariableValue(getTargetVariable());

        // Evaluate function arguments
        long[] argv = new long[functionArguments.size()];
        for (int i = 0; i < functionArguments.size(); i++) {
            argv[i] = functionArguments.get(i).eval(context);
        }

        long fnValue = context.invokeOperation(functionName, argv);

        // cycles accounting (keep base unless you track nested cost separately)
        this.currentCyclesNumber = InstructionData.JUMP_EQUAL_FUNCTION.getCycles();

        return (leftValue == fnValue) ? jumpTargetLabel : FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String shown = (displayName != null) ? displayName : functionName;
        String args = functionArguments.stream().map(QuoteArg::render).collect(Collectors.joining(","));
        return "IF " + getTargetVariable().getRepresentation()
                + " = (" + shown + (args.isEmpty() ? "" : "," + args) + ")"
                + " GOTO " + jumpTargetLabel.getLabelRepresentation();
    }

    @Override
    public List<Instruction> getInnerInstructions() {
        return innerInstructions;
    }

    @Override
    public int getCycleOfInstruction() {
        return (currentCyclesNumber == 0)
                ? InstructionData.JUMP_EQUAL_FUNCTION.getCycles()
                : currentCyclesNumber;
    }

    @Override
    public int getMaxDegree() {
        return MAX_DEGREE;
    }

    @Override
    public Label getReferenceLabel() {
        return jumpTargetLabel;
    }


    @Override
    public int expandInstruction(int startNumber) {
        innerInstructions.clear();

        // fresh work variable for function result
        Variable tmp = getProgramOfThisInstruction().generateUniqueVariable();

        int n = startNumber;
        Label carry = (getLabel() == null) ? FixedLabel.EMPTY : getLabel();

        // 1) tmp <- (f, args...)
        innerInstructions.add(
                new QuoteInstruction(tmp, carry, this, n++, functionName, deepCopyArgs(functionArguments))
        );

        // 2) IF target == tmp GOTO jumpTargetLabel
        innerInstructions.add(
                new JumpEqualVariableInstruction(getTargetVariable(), FixedLabel.EMPTY, tmp, jumpTargetLabel, this, n++)
        );

        return n;
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber,
                                     Map<Variable, Variable> varMap,
                                     Map<Label, Label> labelMap,
                                     Instruction newOrigin,
                                     OperationView newOwner) {

        Variable newTarget = RemapUtils.mapVar(varMap, getTargetVariable());
        Label newLabel = RemapUtils.mapLbl(labelMap, getLabel());
        Label newJumpTarget = RemapUtils.mapLbl(labelMap, jumpTargetLabel);

        // Remap variables inside arguments; keep display names
        List<QuoteArg> remappedArgs = remapArgs(functionArguments, varMap);

        JumpEqualFunctionInstruction cloned = new JumpEqualFunctionInstruction(
                newTarget, newLabel, newJumpTarget, newOrigin, newInstructionNumber,
                this.functionName, remappedArgs
        );
        cloned.setDisplayName(this.displayName);
        cloned.setProgramOfThisInstruction(newOwner);
        return cloned;
    }

    // ---------- helpers ----------
    private static List<QuoteArg> deepCopyArgs(List<QuoteArg> src) {
        List<QuoteArg> out = new ArrayList<>(src.size());
        for (QuoteArg qa : src) {
            if (qa instanceof VarArg va) {
                out.add(new VarArg(va.getVariable()));
            } else if (qa instanceof CallArg call) {
                List<QuoteArg> kids = deepCopyArgs(call.getArgs());
                CallArg cpy = new CallArg(call.getCallName(), kids);
                cpy.setDisplayName(call.getDisplayName());
                out.add(cpy);
            }
        }
        return out;
    }

    private static List<QuoteArg> remapArgs(List<QuoteArg> src, Map<Variable, Variable> varMap) {
        List<QuoteArg> out = new ArrayList<>(src.size());
        for (QuoteArg qa : src) {
            if (qa instanceof VarArg va) {
                Variable mapped = RemapUtils.mapVar(varMap, va.getVariable());
                out.add(new VarArg(mapped));
            } else if (qa instanceof CallArg call) {
                List<QuoteArg> kids = remapArgs(call.getArgs(), varMap);
                CallArg cpy = new CallArg(call.getCallName(), kids);
                cpy.setDisplayName(call.getDisplayName());
                out.add(cpy);
            }
        }
        return out;
    }
}

