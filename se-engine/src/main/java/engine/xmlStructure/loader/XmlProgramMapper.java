package engine.xmlStructure.loader;

import engine.instruction.AbstractInstruction;
import engine.instruction.Instruction;
import engine.instruction.basic.DecreaseInstruction;
import engine.instruction.basic.IncreaseInstruction;
import engine.instruction.basic.JumpNotZeroInstruction;
import engine.instruction.basic.NoOpInstruction;
import engine.instruction.synthetic.*;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.label.LabelImpl;
import engine.program.Program;
import engine.program.ProgramImpl;
import engine.variable.Variable;
import engine.variable.VariableImpl;
import engine.variable.VariableType;
import engine.xmlStructure.generated.SInstruction;
import engine.xmlStructure.generated.SInstructionArgument;
import engine.xmlStructure.generated.SProgram;

import java.util.List;
import java.util.Locale;

final class XmlProgramMapper {

    private XmlProgramMapper() {}

    static Program map(SProgram sProgram) {
        String programName = safeTrim(sProgram.getName());
        ProgramImpl targetProgram = new ProgramImpl(programName != null ? programName : "Unnamed");

        List<SInstruction> sInstructions = sProgram.getSInstructions().getSInstruction();

        if (sInstructions == null || sInstructions.isEmpty()) {
            return targetProgram;
        }

        for (int i = 0; i < sInstructions.size(); i++) {
            SInstruction sInstruction = sInstructions.get(i);
            Instruction mapped = mapSingleInstruction(sInstruction, i + 1);
            targetProgram.addInstruction(mapped);
        }

        return targetProgram;
    }

    private static Instruction mapSingleInstruction(SInstruction sInstruction, int ordinal) {
        try {
            String instructionName = toUpperSafe(sInstruction.getName());
            Label instructionLabel = parseLabel(sInstruction.getSLabel(), instructionName, ordinal);
            Variable targetVariable = parseVariable(sInstruction.getSVariable(), instructionName, ordinal);
            List<SInstructionArgument> sInstructionArguments = (sInstruction.getSInstructionArguments() != null) ?
                    sInstruction.getSInstructionArguments().getSInstructionArgument() :
                    null;

            return createNewInstruction(instructionName, instructionLabel, targetVariable, sInstructionArguments, ordinal);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static AbstractInstruction createNewInstruction(String instructionName,
                                                            Label instructionLabel,
                                                            Variable targetVariable,
                                                            List<SInstructionArgument> sInstructionArguments,
                                                            int ordinal) {
        switch (instructionName) {
            case "INCREASE":
                return new IncreaseInstruction(targetVariable, instructionLabel);

            case "DECREASE":
                return new DecreaseInstruction(targetVariable, instructionLabel);

            case "JUMP_NOT_ZERO": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new JumpNotZeroInstruction(targetVariable, instructionLabel, addedLabel);
            }

            case "NEUTRAL":
                return new NoOpInstruction(targetVariable, instructionLabel);

            case "ZERO_VARIABLE":
                return new ZeroVariableInstruction(targetVariable, instructionLabel);

            case "GOTO_LABEL": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new GotoLabelInstruction(targetVariable, instructionLabel, addedLabel);
            }

            case "ASSIGNMENT": {
                String sourceVariableStr = sInstructionArguments.getFirst().getValue();
                Variable sourceVariable = parseVariable(sourceVariableStr, instructionName, ordinal);
                return new AssignmentInstruction(targetVariable, instructionLabel, sourceVariable);
            }

            case "CONSTANT_ASSIGNMENT": {
                int constantValue = Integer.parseInt(sInstructionArguments.getFirst().getValue());
                return new ConstantAssignmentInstruction(targetVariable, instructionLabel, constantValue);
            }

            case "JUMP_ZERO": {
                String targetLabel = sInstructionArguments.getFirst().getValue();
                Label addedLabel = parseLabel(targetLabel, instructionName, ordinal);
                return new JumpZeroInstruction(targetVariable, instructionLabel, addedLabel);
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

                return new JumpEqualConstantInstruction(targetVariable, instructionLabel, constantValue, addedLabel);
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

                return new JumpEqualVariableInstruction(targetVariable, instructionLabel, sourceVariable, addedLabel);
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
        }
        else if (trimmed.matches("L\\d+")) {
            String numberPart = trimmed.substring(1);
            int labelNumber = Integer.parseInt(numberPart);
            label = new LabelImpl(labelNumber);
        }
        else {
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
            if(prefix.equalsIgnoreCase("x")) variableType = VariableType.INPUT;
            else if(prefix.equalsIgnoreCase("z")) variableType = VariableType.WORK;
            else throw new IllegalArgumentException(
                    "Unsupported variable type: " + prefix + "for " + where + " at instruction number " + ordinal);

            return new VariableImpl(variableType, number);
        }

        throw new IllegalArgumentException(
                "Problem creating the variable: " + trimmed + ". expected x1, x2... or z1, z2... or y" + System.lineSeparator() +
                        "Instruction number: " + ordinal + System.lineSeparator() +
                        "Instruction name: " + where
        );
    }

    private static String safeTrim(String s) { return s == null ? null : s.trim(); }

    private static String toUpperSafe(String s) { return s == null ? null : s.trim().toUpperCase(Locale.ROOT); }
}
