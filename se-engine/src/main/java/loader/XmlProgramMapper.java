package loader;

import instruction.AbstractInstruction;
import instruction.Instruction;
import instruction.OriginOfAllInstruction;
import label.FixedLabel;
import label.Label;
import label.LabelImpl;
import operation.Operation;
import program.Program;
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

final class XmlProgramMapper {

    private XmlProgramMapper() {}

    static Operation map(SProgram sProgram) {
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
        if (sInstructions == null || sInstructions.isEmpty()) {
            // Build an empty program consistently via Builder
            return new ProgramImpl.Builder()
                    .withName(programName)
                    .withInstructions(code)
                    .withVariables(vars)
                    .withLabels(labels)
                    .withEntry(FixedLabel.EMPTY)
                    .build();
        }

        for (int i = 0; i < sInstructions.size(); i++) {
            SInstruction sInstruction = sInstructions.get(i);
            Instruction mapped = mapSingleInstruction(sInstruction, i + 1);

            // collect instruction
            code.add(mapped);

            // collect label used on the instruction line (if any)
            Label lbl = mapped.getLabel();
            if (lbl != null && lbl != FixedLabel.EMPTY && !labels.contains(lbl)) {
                labels.add(lbl);
            }

            // collect variables visible on the instruction
            if (mapped.getTargetVariable() != null) vars.add(mapped.getTargetVariable());
            if (mapped.getSourceVariable() != null) vars.add(mapped.getSourceVariable());
        }

        // ---- build ProgramImpl via Builder (so bucketing happens in ProgramImpl’s ctor) ----
        return new ProgramImpl.Builder()
                .withName(programName)
                .withInstructions(code)
                .withVariables(vars)
                .withLabels(labels)
                .withEntry(labels.isEmpty() ? FixedLabel.EMPTY : labels.getFirst())
                .build();
    }

    private static Instruction mapSingleInstruction(SInstruction sInstruction, int ordinal) {
        try {
            String instructionName = toUpperSafe(sInstruction.getName());

            Label instructionLabel   = parseLabel(sInstruction.getLabel(), instructionName, ordinal);
            Variable targetVariable  = parseVariable(sInstruction.getVariable(), instructionName, ordinal);

            List<SInstructionArgument> sInstructionArguments = sInstruction.getArguments();
            Instruction originInstruction = new OriginOfAllInstruction();

            return createNewInstruction(
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

    private static AbstractInstruction createNewInstruction(String instructionName,
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

            }

            default:
                throw new IllegalArgumentException(
                        "Unknown instruction name at position " + ordinal + ": " + instructionName
                );
        }
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

    private static String safeTrim(String s) { return s == null ? null : s.trim(); }
    private static String toUpperSafe(String s) { return s == null ? null : s.trim().toUpperCase(Locale.ROOT); }
}
