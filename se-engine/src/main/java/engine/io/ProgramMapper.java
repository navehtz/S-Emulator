package engine.io;

import engine.domain.Program;
import engine.domain.Instruction;
import engine.domain.InstructionArgument;
import engine.domain.Kind;

import engine.generated.*;

import java.util.ArrayList;
import java.util.List;

public final class ProgramMapper {

    private ProgramMapper() {}

    // TODO: change name of this method
    public static Program map(SProgram sourceProgram) {
        if (sourceProgram == null) {
            throw new IllegalArgumentException("sourceProgram is null");
        }

        String programName = requireTrimmed(sourceProgram.getName(), "program.name");

        List<Instruction> instructions = mapInstructions(sourceProgram);
        List<String> inputVariablesOrdered = extractInputVariables(sourceProgram);
        List<String> labelsOrdered = getOrderedUniqueLabels(sourceProgram);

        return new Program(programName, instructions, inputVariablesOrdered, labelsOrdered);
    }


    private static List<Instruction> mapInstructions(SProgram sourceProgram) {
        List<Instruction> instructions = new ArrayList<>();
        List<SInstruction> sourceInstructionList = getSourceInstructionList(sourceProgram);

        int lineNumberOneBased = 1;
        for (SInstruction sourceInstruction : sourceInstructionList) {
            instructions.add(mapInstruction(sourceInstruction, lineNumberOneBased++));
        }
        return instructions;
    }


    private static List<String> extractInputVariables(SProgram sourceProgram) {
        List<String> inputVariablesOrdered = new ArrayList<>();
        List<SInstruction> sourceInstructionList = getSourceInstructionList(sourceProgram);

        for (SInstruction sourceInstruction : sourceInstructionList) {
            addInputIfValid(inputVariablesOrdered, sourceInstruction.getSVariable());

            SInstructionArguments args = sourceInstruction.getSInstructionArguments();
            if (args != null) {
                for (SInstructionArgument arg : args.getSInstructionArgument()) {
                    addInputIfValid(inputVariablesOrdered, arg.getValue());
                }
            }
        }
        return inputVariablesOrdered;
    }

    // TODO: put the label EXIT in the end of the list
    private static List<String> getOrderedUniqueLabels (SProgram sourceProgram) {
        List<String> labelsOrdered = new ArrayList<>();
        List<SInstruction> sourceInstructionList = getSourceInstructionList(sourceProgram);

        for (SInstruction sourceInstruction : sourceInstructionList) {
            String rawLabel = sourceInstruction.getSLabel();
            if (rawLabel != null && !rawLabel.isBlank()) {
                labelsOrdered.add(rawLabel.trim());
            }
        }
        return labelsOrdered;
    }


    private static List<SInstruction> getSourceInstructionList(SProgram sourceProgram) {
        SInstructions sourceInstructions = sourceProgram.getSInstructions();

        if (sourceInstructions == null) {
            return List.of();
        }

        List<SInstruction> instructionList = sourceInstructions.getSInstruction();

        if (instructionList == null) {
            return List.of();
        }

        return instructionList;
    }


    private static void addInputIfValid(List<String> inputVariables, String candidateVariableName) {
        if (candidateVariableName == null || candidateVariableName.isBlank()) {
            return;
        }

        String trimmedVariableName = candidateVariableName.trim();

        boolean isInputVariable = trimmedVariableName.charAt(0) == 'x';

        if (isInputVariable && !inputVariables.contains(trimmedVariableName)) {
            inputVariables.add(trimmedVariableName);
        }
    }

    private static Instruction mapInstruction(SInstruction sourceInstruction, int lineNumber1Based) {
        if (sourceInstruction == null) {
            throw new IllegalArgumentException("sourceInstruction is null");
        }
        if (lineNumber1Based < 1) {
            throw new IllegalArgumentException("lineNumber1Based must be >= 1");
        }

        Kind kind = mapKindFromType(sourceInstruction.getType());
        String label = sourceInstruction.getSLabel() == null ? "" : sourceInstruction.getSLabel().trim();
        String command = buildCommandText(sourceInstruction);
        int cycles = computeCycles(sourceInstruction.getName());

        return new Instruction(lineNumber1Based, kind, label, command, cycles);
    }

    private static Kind mapKindFromType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }

        String typeStrUpper = type.toUpperCase();
        return switch(typeStrUpper) {
            case "B", "BASIC" -> Kind.BASIC;
            case "S", "SYNTHETIC" -> Kind.SYNTHETIC;
            default -> throw new IllegalArgumentException("unknown type " + type);
        };
    }

    // TODO: create a class of command which will have: name, compute, cycles number
    private static String buildCommandText(SInstruction sourceInstruction) {
        String variable = sourceInstruction.getSVariable().trim();
        String label = sourceInstruction.getSLabel() == null ? "" : sourceInstruction.getSLabel().trim();
        List<InstructionArgument> args = mapArguments(sourceInstruction.getSInstructionArguments());

        String a1 = args.size() > 0 ? args.get(0).getValue() : "";
        String a2 = args.size() > 1 ? args.get(1).getValue() : "";

        return switch (sourceInstruction.getName()) {
            case "INCREASE"              -> String.format("%s <- %s + 1", variable, variable);
            case "DECREASE"              -> String.format("%s <- %s - 1", variable, variable);
            case "JUMP_NOT_ZERO"         -> String.format("IF %s != 0 GOTO %s", variable, label);
            case "NEUTRAL"               -> String.format("%s <- %s", variable, variable);
            case "ZERO_VARIABLE"         -> String.format("%s <- 0", variable);
            case "GOTO_LABEL"            -> String.format("GOTO %s", label);
            case "ASSIGNMENT"            -> String.format("%s <- %s", variable, a1);
            case "CONSTANT_ASSIGNMENT"   -> String.format("%s <- %s", variable, a1);
            case "JUMP_ZERO"             -> String.format("IF %s = 0 GOTO %s", variable, label);
            case "JUMP_EQUAL_CONSTANT"   -> String.format("IF %s = %s GOTO %s", variable, a1, label);
            case "JUMP_EQUAL_VARIABLE"   -> String.format("IF %s = %s GOTO %s", variable, a1, label);
            default -> throw new IllegalArgumentException("Unknown instruction name: " + sourceInstruction.getName());
        };
    }

    // TODO: change to a map (commandName to number of cycles
    private static int computeCycles(String commandName) {
        if (commandName == null || commandName.isBlank()) {
            throw new IllegalArgumentException("instruction name is null or blank");
        }

        return switch (commandName.trim().toUpperCase()) {
            case "INCREASE"             -> 1;
            case "DECREASE"             -> 1;
            case "JUMP_NOT_ZERO"        -> 2;
            case "NEUTRAL"              -> 0;
            case "ZERO_VARIABLE"        -> 1;
            case "GOTO_LABEL"           -> 1;
            case "ASSIGNMENT"           -> 4;
            case "CONSTANT_ASSIGNMENT"  -> 2;
            case "JUMP_ZERO"            -> 2;
            case "JUMP_EQUAL_CONSTANT"  -> 2;
            case "JUMP_EQUAL_VARIABLE"  -> 2;
            default -> throw new IllegalArgumentException("Unknown instruction name: " + commandName);
        };
    }


    private static List<InstructionArgument> mapArguments(SInstructionArguments sourceArguments) {
        if  (sourceArguments == null) {
            return List.of();
        }

        List<SInstructionArgument> rawList = sourceArguments.getSInstructionArgument();

        if(rawList == null || rawList.isEmpty()) {
            return List.of();
        }

        List<InstructionArgument> result = new java.util.ArrayList<>(rawList.size());

        for (SInstructionArgument sourceArgument : rawList) {
            if (sourceArgument == null) {
                throw new IllegalArgumentException("argument item is null");
            }
            result.add(mapArgument(sourceArgument));
        }

        return java.util.Collections.unmodifiableList(result);
    }


    private static InstructionArgument mapArgument(SInstructionArgument sourceArgument) {
        if (sourceArgument == null) {
            throw new IllegalArgumentException("sourceArgument is null");
        }

        String name = requireTrimmed(sourceArgument.getName(), "argument.name");
        String value = requireTrimmed(sourceArgument.getValue(), "argument.value");

        return new InstructionArgument(name, value);
    }


    private static String requireTrimmed(String s, String field) {
        if (s == null) {
            throw new IllegalArgumentException(field + " is null");
        }

        String trimmed = s.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " is empty");
        }

        return trimmed;
    }


    private static Kind mapKind(SInstruction sourceInstruction) {
        if (sourceInstruction == null) {
            throw new IllegalArgumentException("sourceInstruction is null");
        }

        String type = sourceInstruction.getType();
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Instruction type is null or blank");
        }

        return mapKindFromType(type);
    }
}
