package loader;

import function.FunctionImpl;
import generatedFromXml.SFunction;
import instruction.AbstractInstruction;
import instruction.Instruction;
import instruction.OriginOfAllInstruction;
import instruction.synthetic.quoteArg.CallArg;
import instruction.synthetic.quoteArg.QuoteArg;
import instruction.synthetic.quoteArg.VarArg;
import label.FixedLabel;
import label.Label;
import label.LabelImpl;
import operation.Operation;
import program.ProgramImpl;
import variable.Variable;
import variable.VariableImpl;
import variable.VariableType;
import generatedFromXml.SInstruction;
import generatedFromXml.SInstructionArgument;
import generatedFromXml.SProgram;
import instruction.basic.DecreaseInstruction;
import instruction.basic.IncreaseInstruction;
import instruction.basic.JumpNotZeroInstruction;
import instruction.basic.NoOpInstruction;
import instruction.synthetic.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XmlProgramMapper {

    //private static final Map<String, String> functionNameToUserString = new HashMap<>();

    private XmlProgramMapper() {
    }

    static Operation map(SProgram sProgram, String uploaderName) {
        // ---- name ----
        String programName = safeTrim(sProgram.getName());
        if (programName == null || programName.isEmpty()) {
            programName = "Unnamed";
        }

        // ---- collect mapped instructions, variables, labels ----
        final List<Instruction> code = new ArrayList<>();
        final Set<Variable> vars = new LinkedHashSet<>();
        final List<Label> labels = new ArrayList<>();

        List<SInstruction> sInstructions = sProgram.getInstructions();
        Set<String> calledFunctionNames = new HashSet<>();
        if (sInstructions == null || sInstructions.isEmpty()) {
            // Build an empty program consistently via Builder
            return new ProgramImpl.Builder()
                    .withName(programName)
                    .withUserUploaded(uploaderName)
                    .build();
        }

        handleInstructions(calledFunctionNames, code, vars, labels, sInstructions);

        // ---- build ProgramImpl via Builder (so bucketing happens in ProgramImpl’s ctor) ----
        return new ProgramImpl.Builder()
                .withName(programName)
                .withInstructions(code)
                .withVariables(vars)
                .withLabels(labels)
                .withEntry(labels.isEmpty() ? FixedLabel.EMPTY : labels.getFirst())
                .withUserUploaded(uploaderName)
                .withCalledFunctionNames(calledFunctionNames)
                .build();
    }

    static Operation map(SFunction sFunction, String mainProgramName, String uploaderName) {
        String functionName = safeTrim(sFunction.getName());
        if (functionName == null || functionName.isEmpty()) functionName = "UnnamedFunction";

        final List<Instruction> code = new ArrayList<>();
        final Set<Variable> vars = new LinkedHashSet<>();
        final List<Label> labels = new ArrayList<>();

        List<SInstruction> sInstructions = sFunction.getInstructions();
        Set<String> calledFunctionNames = new HashSet<>();
        if (sInstructions != null) {
            handleInstructions(calledFunctionNames, code, vars, labels, sInstructions);
        }

        String userString = safeTrim(sFunction.getUserString());

        return new FunctionImpl.Builder()
                .withName(functionName)
                .withInstructions(code)
                .withVariables(vars)
                .withLabels(labels)
                .withEntry(labels.isEmpty() ? FixedLabel.EMPTY : labels.getFirst())
                .withUserString(userString)
                .withMainProgramName(mainProgramName)
                .withUserUploaded(uploaderName)
                .withCalledFunctionNames(calledFunctionNames)
                .build();
    }

    private static void handleInstructions(Set<String> calledFunctionNames, List<Instruction> code, Set<Variable> vars, List<Label> labels, List<SInstruction> sInstructions) {
        for (int i = 0; i < sInstructions.size(); i++) {
            SInstruction sInstruction = sInstructions.get(i);
            Instruction mappedInstruction = mapSingleInstruction(calledFunctionNames, sInstruction, i + 1);

            // collect instruction
            code.add(mappedInstruction);

            // collect label used on the instruction line (if any)
            Label lbl = mappedInstruction.getLabel();
            if (lbl != null && lbl != FixedLabel.EMPTY && !labels.contains(lbl)) {
                labels.add(lbl);
            }

            // collect variables visible on the instruction
            if (mappedInstruction.getTargetVariable() != null) vars.add(mappedInstruction.getTargetVariable());
            if (mappedInstruction.getSourceVariable() != null) vars.add(mappedInstruction.getSourceVariable());

            if (mappedInstruction instanceof instruction.synthetic.QuoteInstruction qi) {
                collectVarsFromQuoteArgs(qi.getFunctionArguments(), vars);
            }
        }
    }

    private static void collectVarsFromQuoteArgs(List<QuoteArg> args, Set<Variable> vars) {
        if (args == null) return;
        for (QuoteArg quoteArg : args) {
            if (quoteArg instanceof VarArg v) {
                vars.add(v.getVariable());
            } else if (quoteArg instanceof CallArg c) {
                collectVarsFromQuoteArgs(c.getArgs(), vars);
            }
        }
    }

    private static Instruction mapSingleInstruction(Set<String> calledFunctionNames, SInstruction sInstruction, int ordinal) {
        try {
            String instructionName = toUpperSafe(sInstruction.getName());

            Label instructionLabel = parseLabel(sInstruction.getLabel(), instructionName, ordinal);
            Variable targetVariable = parseVariable(sInstruction.getVariable(), instructionName, ordinal);

            List<SInstructionArgument> sInstructionArguments = sInstruction.getArguments();
            Instruction originInstruction = new OriginOfAllInstruction();

            return createNewInstruction(
                    calledFunctionNames,
                    instructionName,
                    instructionLabel,
                    targetVariable,
                    sInstructionArguments != null ? sInstructionArguments : List.of(),
                    ordinal,
                    originInstruction
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed mapping instruction at #" + ordinal + ": " + e.getMessage(), e);
        }
    }

    private static AbstractInstruction createNewInstruction(Set<String> calledFunctionNames,
                                                            String instructionName,
                                                            Label instructionLabel,
                                                            Variable targetVariable,
                                                            List<SInstructionArgument> sInstructionArguments,
                                                            int ordinal,
                                                            Instruction originInstruction) {
        switch (instructionName) {
            case "INCREASE":
                return new IncreaseInstruction(targetVariable, instructionLabel, originInstruction, ordinal);

            case "DECREASE":
                return new DecreaseInstruction(targetVariable, instructionLabel, originInstruction, ordinal);

            case "JUMP_NOT_ZERO": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new JumpNotZeroInstruction(targetVariable, instructionLabel, addedLabel, originInstruction, ordinal);
            }

            case "NEUTRAL":
                return new NoOpInstruction(targetVariable, instructionLabel, originInstruction, ordinal);

            case "ZERO_VARIABLE":
                return new ZeroVariableInstruction(targetVariable, instructionLabel, originInstruction, ordinal);

            case "GOTO_LABEL": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new GotoLabelInstruction(targetVariable, instructionLabel, addedLabel, originInstruction, ordinal);
            }

            case "ASSIGNMENT": {
                String sourceVariableStr = sInstructionArguments.getFirst().getValue();
                Variable sourceVariable = parseVariable(sourceVariableStr, instructionName, ordinal);
                return new AssignmentInstruction(targetVariable, instructionLabel, sourceVariable, originInstruction, ordinal);
            }

            case "CONSTANT_ASSIGNMENT": {
                int constantValue = Integer.parseInt(sInstructionArguments.getFirst().getValue());
                return new ConstantAssignmentInstruction(targetVariable, instructionLabel, constantValue, originInstruction, ordinal);
            }

            case "JUMP_ZERO": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new JumpZeroInstruction(targetVariable, instructionLabel, addedLabel, originInstruction, ordinal);
            }

            case "JUMP_EQUAL_CONSTANT": {
                Label addedLabel = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("JEConstantLabel"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .map(labelStr -> parseLabel(labelStr, instructionName, ordinal))
                        .orElseThrow(() -> new IllegalArgumentException("JEConstantLabel not found"));

                long constantValue = Long.parseLong(
                        sInstructionArguments.stream()
                                .filter(arg -> arg.getName().equalsIgnoreCase("constantValue"))
                                .map(SInstructionArgument::getValue)
                                .findFirst()
                                .get());

                return new JumpEqualConstantInstruction(targetVariable, instructionLabel, constantValue, addedLabel, originInstruction, ordinal);
            }

            case "JUMP_EQUAL_VARIABLE": {
                Label addedLabel = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("JEVariableLabel"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .map(labelStr -> parseLabel(labelStr, instructionName, ordinal))
                        .orElseThrow(() -> new IllegalArgumentException("JEVariableLabel not found"));

                Variable sourceVariable = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("variableName"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .map(labelStr -> parseVariable(labelStr, instructionName, ordinal))
                        .orElseThrow(() -> new IllegalArgumentException("variableName not found"));

                return new JumpEqualVariableInstruction(targetVariable, instructionLabel, sourceVariable, addedLabel, originInstruction, ordinal);
            }

            case "QUOTE": {
                String functionName = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("functionName"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("functionName not found"));

                String rawFunctionArguments = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("functionArguments"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .orElse("");

                functionName = normalizeKey(functionName);
                calledFunctionNames.add(functionName);
                List<QuoteArg> functionArguments = parseTopLevelArgs(calledFunctionNames, rawFunctionArguments, instructionName, ordinal);

                return new QuoteInstruction(targetVariable, instructionLabel, originInstruction, ordinal, functionName, functionArguments);
            }

            case "JUMP_EQUAL_FUNCTION": {
                Label addedLabel = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("JEFunctionLabel"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .map(labelStr -> parseLabel(labelStr, instructionName, ordinal))
                        .orElseThrow(() -> new IllegalArgumentException("JEFunctionLabel not found"));

                String functionName = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("functionName"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("functionName not found"));

                String rawFunctionArguments = sInstructionArguments.stream()
                        .filter(arg -> arg.getName().equalsIgnoreCase("functionArguments"))
                        .map(SInstructionArgument::getValue)
                        .findFirst()
                        .orElse("");

                functionName = normalizeKey(functionName);
                calledFunctionNames.add(functionName);
                List<QuoteArg> functionArguments = parseTopLevelArgs(calledFunctionNames, rawFunctionArguments, instructionName, ordinal);

                return new JumpEqualFunctionInstruction(targetVariable, instructionLabel, addedLabel, originInstruction, ordinal, functionName, functionArguments);
            }

            default:
                throw new IllegalArgumentException(
                        "Unknown instruction name at position " + ordinal + ": " + instructionName
                );
        }
    }

    public static String normalizeKey(String name) {
        String s = safeTrim(name);
        if (s == null) throw new IllegalArgumentException("Null program/function name");
        return s;
    }

    private static List<QuoteArg> parseTopLevelArgs(Set<String> calledFunctionNames, String rawFunctionArguments, String where, int ordinal) {
        List<String> tokens = splitTopLevel(rawFunctionArguments);
        List<QuoteArg> functionArguments = new ArrayList<>();
        for (String token : tokens) {
            functionArguments.add(parseArgExpression(calledFunctionNames, token, where, ordinal));
        }

        return functionArguments;
    }

    private static QuoteArg parseArgExpression(Set<String> calledFunctionNames, String token, String where, int ordinal) {
        String trimmedToken = safeTrim(token);
        if (trimmedToken == null || trimmedToken.isEmpty()) {
            throw new IllegalArgumentException("Empty argument for \" + where + \" at #\" + ordinal");
        }

        if (trimmedToken.charAt(0) == '(') {
            if (trimmedToken.charAt(trimmedToken.length() - 1) != ')') {
                throw new IllegalArgumentException("Unbalanced parentheses in arg: " + token);
            }
            String itemsInside = trimmedToken.substring(1, trimmedToken.length()-1).trim();
            List<String> parts = splitTopLevel(itemsInside);

            if (parts.isEmpty()) {
                throw new IllegalArgumentException("Empty call in arg: " + token);
            }

            //String functionUserString = functionNameToUserString.get(parts.getFirst().trim());
            String functionName = parts.getFirst().trim();
            functionName = normalizeKey(functionName);
            calledFunctionNames.add(functionName);
            List<QuoteArg> subArguments = new ArrayList<>();

            for (int i = 1; i < parts.size(); i++) {
                subArguments.add(parseArgExpression(calledFunctionNames, parts.get(i), where, ordinal));
            }

            return new CallArg(functionName, subArguments);
        } else {
            Variable variable = parseVariable(trimmedToken, where, ordinal);
            return new VarArg(variable);
        }

    }

    private static List<String> splitTopLevel(String s) {
        List<String> parts = new ArrayList<>();
        if (s == null) return parts;
        int depth = 0;
        StringBuilder currentSB = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                depth++;
                currentSB.append(c);
            } else if (c == ')') {
                depth--;
                currentSB.append(c);
            } else if (depth == 0 && c == ',') {
                parts.add(currentSB.toString().trim());
                currentSB.setLength(0);
            } else {
                currentSB.append(c);
            }
        }
        if (!currentSB.isEmpty())
            parts.add(currentSB.toString().trim());

        parts.removeIf(String::isEmpty);

        return parts;
    }

    private static Label parseLabel(String raw, String where, int ordinal) {
        String trimmed = safeTrim(raw);
        if (trimmed == null || trimmed.isEmpty()) {
            return FixedLabel.EMPTY;
        }

        Label label;
        if (trimmed.equalsIgnoreCase(FixedLabel.EXIT.getLabelRepresentation())) {
            label = FixedLabel.EXIT;
        } else if (trimmed.matches("L\\d+")) {
            String numberPart = trimmed.substring(1);
            int labelNumber = Integer.parseInt(numberPart);
            label = new LabelImpl(labelNumber);
        } else {
            throw new IllegalArgumentException(
                    "Problem creating the label: " + raw + System.lineSeparator() +
                            "Instruction number: " + ordinal + System.lineSeparator() +
                            "Instruction name: " + where
            );
        }

        return label;
    }

    private static Variable parseVariable(String token, String where, int ordinal) {
        String trimmed = safeTrim(token);
        if (trimmed == null || trimmed.isEmpty()) return null;

        if (trimmed.equalsIgnoreCase("y")) {
            return new VariableImpl(VariableType.RESULT, 1);
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("^([xz])(\\d+)$", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(trimmed);
        if (matcher.matches()) {
            String prefix = matcher.group(1);
            int number = Integer.parseInt(matcher.group(2));

            VariableType variableType;
            if (prefix.equalsIgnoreCase("x")) variableType = VariableType.INPUT;
            else if (prefix.equalsIgnoreCase("z")) variableType = VariableType.WORK;
            else throw new IllegalArgumentException(
                        "Unsupported variable type: " + prefix + " for " + where + " at instruction number " + ordinal);

            return new VariableImpl(variableType, number);
        }

        throw new IllegalArgumentException(
                "Problem creating the variable: " + trimmed + ". expected x / y / z" + System.lineSeparator() +
                        "The problem occurred at instruction number: " + ordinal + " named: " + where
        );
    }

    public static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    private static String toUpperSafe(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }

    private static Set<Variable> extractXVariablesIntoSet(Set<Variable> seenInputVariable, String functionArguments) {
        Pattern pattern = Pattern.compile("x(\\d+)");
        Matcher matcher = pattern.matcher(functionArguments);

        while (matcher.find()) {
            int number = Integer.parseInt(matcher.group(1));
            Variable newVariable = new VariableImpl(VariableType.INPUT, number);

            seenInputVariable.add(newVariable);
        }

        return seenInputVariable;
    }
}
