package instruction.synthetic;

import engine.ProgramRegistry;
import execution.ExecutionContext;
import function.Function;
import instruction.*;
import instruction.synthetic.quoteArg.CallArg;
import instruction.synthetic.quoteArg.QuoteArg;
import instruction.synthetic.quoteArg.VarArg;
import label.FixedLabel;
import label.Label;
import operation.Operation;
import operation.OperationView;
import variable.Variable;
import instruction.synthetic.functionExecutionUtils.FunctionInstructionUtils;

import java.util.*;

public class QuoteInstruction extends AbstractInstruction implements LabelReferencesInstruction, SyntheticInstruction {

    private static final int MAX_DEGREE = 5;

    private final String functionName;
    private String displayName;
    private final List<QuoteArg> functionArguments;

    private final List<Instruction> innerInstructions = new ArrayList<>();
    private final Map<Variable, Variable> variableToNewVariableMap = new LinkedHashMap<>();
    private final Map<Label, Label> labelToNewLabelMap = new LinkedHashMap<>();
    private int currentCyclesNumber;

    public QuoteInstruction(Variable targetVariable, Instruction origin, int instructionNumber, String functionName, List<QuoteArg> functionArguments) {
        super(InstructionData.QUOTE, InstructionType.SYNTHETIC, targetVariable, FixedLabel.EMPTY, origin, instructionNumber);
        this.functionName = Objects.requireNonNull(functionName, "functionName");
        this.functionArguments = functionArguments != null ? functionArguments : List.of();
    }

    public QuoteInstruction(Variable targetVariable, Label label, Instruction origin, int instructionNumber, String functionName, List<QuoteArg> functionArguments) {
        super(InstructionData.QUOTE, InstructionType.SYNTHETIC, targetVariable, label, origin, instructionNumber);
        this.functionName = functionName;
        this.functionArguments = functionArguments != null ? functionArguments : List.of();
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<QuoteArg> getFunctionArguments() {
        return functionArguments;
    }

    public void setDisplayName(String displayName) {
        this.displayName = (displayName == null || displayName.isBlank())
                ? null : displayName;
    }

    @Override
    public Instruction createInstructionWithInstructionNumber(int instructionNumber) {
        QuoteInstruction clonedQuoteInstruction = new QuoteInstruction(getTargetVariable(), getLabel(), getOriginalInstruction(), instructionNumber, functionName, copyArgs(functionArguments));

        clonedQuoteInstruction.setDisplayName(this.displayName);
        return clonedQuoteInstruction;
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

        for (int i = 0; i < functionArguments.size(); i++) {
            argsValues[i] = functionArguments.get(i).eval(context);
        }

        long functionResult = context.invokeOperation(functionName, argsValues);
        context.updateVariable(getTargetVariable(), functionResult);
        int calleeCycles = context.getLastInvocationCycles();

        this.currentCyclesNumber = InstructionData.QUOTE.getCycles() + calleeCycles;
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String shownName = (displayName != null && !displayName.isBlank())
                ? displayName
                : resolveUserString(functionName);
        if (shownName == null || shownName.isBlank()) {
            shownName = functionName;
        }
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
    public int getCycleOfInstruction() {
        return (currentCyclesNumber == 0) ? InstructionData.QUOTE.getCycles() : currentCyclesNumber;
    }

    @Override
    public int expandInstruction(int startNumber) {
        List<Instruction> expandedInstructions = convertFunctionData(startNumber);
        innerInstructions.clear();
        innerInstructions.addAll(expandedInstructions);
        return startNumber + innerInstructions.size();
    }

    private List<Instruction> convertFunctionData(int startNumber) {
        List<Instruction> expandedInstructions = new ArrayList<>();
        Function calleeFunction = resolveCallee();

        mapQuoteFunctionVariables(calleeFunction);
        mapQuoteFunctionLabels(calleeFunction);

        int instructionNumber = startNumber;
        instructionNumber = addInstructionsForParameters(expandedInstructions, instructionNumber, calleeFunction);
        instructionNumber = addRemappedClonedFunctionInstructions(expandedInstructions, instructionNumber, calleeFunction);
        addResultAssignment(expandedInstructions, instructionNumber);

        return expandedInstructions;
    }

    private Function resolveCallee() {
        // Resolve through the owning operation's registry.
        // If your ProgramImpl is the owner: ((ProgramImpl)getProgramOfThisInstruction()).getRegistry().getByName(name)
        // and cast to Function.
        OperationView owner = getProgramOfThisInstruction(); // set in Operation.addInstruction(...)
        if (owner == null) throw new IllegalStateException("QuoteInstruction has no owning Operation");
        ProgramRegistry reg = owner.getRegistry();
        if (reg == null && owner instanceof Operation) {
            throw new IllegalStateException("No ProgramRegistry bound to owning Operation: " +
                    owner.getName() + ". Please ensure registry is properly set after cloning operations.");
        }
        if (reg == null) {
            throw new IllegalStateException("No ProgramRegistry bound to owning Operation");
        }
        OperationView callee;
        try {
            callee = reg.getProgramByName(functionName);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Cannot resolve function: " + functionName, e);
        }
        if (callee == null) throw new IllegalStateException("Cannot resolve function: " + functionName);
        return (Function) callee;
    }

    private void mapQuoteFunctionVariables(Function calleeFunction) {
        variableToNewVariableMap.put(Variable.RESULT, getProgramOfThisInstruction().generateUniqueVariable());

        List<Variable> inputs = new ArrayList<>(calleeFunction.getInputVariables());
        inputs.sort(Comparator.comparing(Variable::getIndex));
        for (Variable x : inputs) {
            variableToNewVariableMap.put(x, getProgramOfThisInstruction().generateUniqueVariable());
        }

        for (Variable z : calleeFunction.getWorkVariables()) {
            variableToNewVariableMap.put(z, getProgramOfThisInstruction().generateUniqueVariable());
        }
    }

    private void mapQuoteFunctionLabels(Function calleeFunction) {
        // labels attached to lines
        for (Label label : calleeFunction.getLabelsInProgram()) {
            labelToNewLabelMap.put(label, getProgramOfThisInstruction().generateUniqueLabel());
        }
        // labels that exist as targets (from labelToInstruction)
        calleeFunction.getLabelToInstruction().keySet()
                .forEach(lbl -> labelToNewLabelMap.putIfAbsent(lbl, getProgramOfThisInstruction().generateUniqueLabel()));

        // ensure EXIT mapped (we will rewire it)
        labelToNewLabelMap.putIfAbsent(FixedLabel.EXIT, getProgramOfThisInstruction().generateUniqueLabel());
    }

    private int addInstructionsForParameters(List<Instruction> expandedInstructions, int instructionNumber, Function calleeFunction) {
        Label carryLabel = getLabel();
        boolean first = (carryLabel != null && carryLabel != FixedLabel.EMPTY);

        List<Variable> inputs = new ArrayList<>(calleeFunction.getInputVariables());
        inputs.sort(Comparator.comparingInt(Variable::getIndex));

        if (functionArguments.size() != inputs.size()) {
            throw new IllegalArgumentException("QuoteInstruction: expected "
                    + inputs.size() + " arguments, but got " + functionArguments.size()
                    + " for function " + calleeFunction.getName());
        }

        for (int i = 0; i < inputs.size(); i++) {
            Variable calleeXi = inputs.get(i);
            Variable mappedXi = variableToNewVariableMap.get(calleeXi);
            if (mappedXi == null) {
                throw new IllegalStateException("Missing mapped variable for " + calleeXi.getRepresentation());
            }

            Label usedLabel = first ? carryLabel : FixedLabel.EMPTY;
            first = false;

            QuoteArg quoteArg = functionArguments.get(i);
            if (quoteArg instanceof VarArg varArg) {
                var assignmentInstruction = new AssignmentInstruction(mappedXi, usedLabel, varArg.getVariable(), this, instructionNumber++);
                expandedInstructions.add(assignmentInstruction);

            } else if (quoteArg instanceof CallArg callArg) {
                List<QuoteArg> deepArgs = copyArgs(callArg.getArgs());
                QuoteInstruction nestedQuoteInstruction = new QuoteInstruction(mappedXi, usedLabel, this, instructionNumber++, callArg.getCallName(), deepArgs);


                nestedQuoteInstruction.setDisplayName(callArg.getDisplayName());
                expandedInstructions.add(nestedQuoteInstruction);
            } else {
                throw new IllegalStateException("Unknown QuoteArg type: " + quoteArg.getClass().getSimpleName());
            }
        }
        return instructionNumber;
    }

    private int addRemappedClonedFunctionInstructions(List<Instruction> expandedInstructions, int instructionNumber, Function calleeFunction) {
        for (Instruction innerInstruction : calleeFunction.getInstructionsList()) {
            Instruction clonedInstruction = innerInstruction.remapAndClone(
                    instructionNumber++,
                    variableToNewVariableMap,
                    labelToNewLabelMap,
                    this,
                    getProgramOfThisInstruction()
            );
            expandedInstructions.add(clonedInstruction);
        }
        return instructionNumber;
    }

    private void addResultAssignment(List<Instruction> expandedInstructions, int instructionNumber) {
        Variable mappedY = variableToNewVariableMap.get(Variable.RESULT);
        if (mappedY == null) {
            throw new IllegalStateException("Function RESULT not mapped");
        }
        Label endLabel = labelToNewLabelMap.getOrDefault(FixedLabel.EXIT, FixedLabel.EMPTY);
        expandedInstructions.add(new AssignmentInstruction(getTargetVariable(), endLabel, mappedY, this, instructionNumber)); //TODO:Check if needed ++
    }

    @Override
    public Instruction remapAndClone(int newInstructionNumber, Map<Variable, Variable> varMap, Map<Label, Label> labelMap, Instruction origin, OperationView mainProgram) {
        Variable tgtLbl = RemapUtils.mapVar(varMap, getTargetVariable());
        Label newLbl = RemapUtils.mapLbl(labelMap, getLabel());
        List<QuoteArg> mappedQuoteArguments = FunctionInstructionUtils.mapFunctionArgumentsToNewList(functionArguments, varMap, true);

        Instruction clonedInstruction = new QuoteInstruction(tgtLbl, newLbl, origin, newInstructionNumber, functionName, mappedQuoteArguments);
        clonedInstruction.setProgramOfThisInstruction(mainProgram);
        return clonedInstruction;
    }

    private String resolveUserString(String calleeName) {
        OperationView owner = getProgramOfThisInstruction();
        if (owner instanceof program.ProgramImpl pr && pr.getRegistry() != null) {
            var op = pr.getRegistry().getProgramByName(calleeName);
            if (op instanceof function.Function f) return f.getUserString();
        }
        return null;
    }

}
