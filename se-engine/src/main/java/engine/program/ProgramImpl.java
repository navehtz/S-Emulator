package engine.program;

import engine.instruction.Instruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;
import engine.variable.VariableType;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProgramImpl implements Program {

    private final String programName;
    private final List<Instruction> programInstructions;
    private final Map<Label, Instruction> labelToInstruction;
    private final Set<Variable> inputVariables = new LinkedHashSet<>();
    private final List<Label> labelsInProgram = new ArrayList<>();

    public ProgramImpl(String name) {
        this.programName = name;
        this.programInstructions = new ArrayList<>();
        this.labelToInstruction = new HashMap<>();
    }

    @Override
    public String getName() {
        return this.programName;
    }

    @Override
    public void addInstruction(Instruction instruction) {
        programInstructions.add(instruction);

        Label currentLabel = instruction.getLabel();
        if(currentLabel != FixedLabel.EMPTY) {                    // Add label and its instruction to map
            labelToInstruction.put(currentLabel, instruction);
            labelsInProgram.add(currentLabel);
        }

        Variable targetVariable = instruction.getTargetVariable();
        if(targetVariable.getType() == VariableType.INPUT) {     // Add inputs variable to list
            inputVariables.add(targetVariable);   // add item to the set only if it is not inside
        }

        Variable sourceVariable = instruction.getSourceVariable();
        if (sourceVariable != null && sourceVariable.getType() == VariableType.INPUT) {
            inputVariables.add(sourceVariable);
        }
    }

    @Override
    public List<Instruction> getInstructionsList() {
        return this.programInstructions;
    }

    @Override
    public int calculateMaxDegree() {
        // TODO
        return -1;
    }

    @Override
    public int calculateCycles() {
        // TODO
        return -1;
    }

    @Override
    public Instruction getInstructionByLabel(Label label) {
        // TODO: TO CHECK IF THE LABEL EXIST
        return labelToInstruction.get(label);
    }

    @Override
    public String programRepresentation() {
        StringBuilder programDisplay = new StringBuilder();

        for(int i = 0; i < programInstructions.size(); i++) {
            Instruction instruction = programInstructions.get(i);
            String line = instruction.instructionRepresentation(i + 1);
            programDisplay.append(line).append(System.lineSeparator());
        }

        return programDisplay.toString();
    }

    @Override
    public Set<Variable> getInputVariables() {
        return this.inputVariables;
    }

    @Override
    public String getProgramDisplay() {
        //ensureProgramLoaded();

        List<String> inputs = displayInputVariable();
        List<String> labels = displayOrderedLabelsExitLast();

        StringBuilder programDisplay = new StringBuilder();
        programDisplay.append("Program: ").append(getName()).append(System.lineSeparator());
        programDisplay.append("Inputs: ").append(String.join(", ", inputs)).append(System.lineSeparator());
        programDisplay.append("Labels: ").append(String.join(", ", labels)).append(System.lineSeparator());
        programDisplay.append("Instructions: ").append(System.lineSeparator());

        for(int i = 0; i < programInstructions.size(); i++) {
            Instruction instruction = programInstructions.get(i);
            String line = instruction.instructionRepresentation(i + 1);
            programDisplay.append(line).append(System.lineSeparator());
        }

        return programDisplay.toString();
    }

    private List<String> displayOrderedLabelsExitLast() {
        List<Label> labels = reorderLabelsExitLast(labelsInProgram);

        return labels.stream()
                .map(Label::getLabelRepresentation)
                .collect(Collectors.toList());
    }

    private List<Label> reorderLabelsExitLast(List<Label> labelsInOrder) {
        return Stream.concat(
                labelsInOrder.stream().filter(l -> !FixedLabel.EXIT.equals(l)),
                labelsInOrder.stream().filter(l ->  FixedLabel.EXIT.equals(l))
        ).collect(Collectors.toList());
    }

    private List<String> displayInputVariable() {
        return inputVariables.stream()
                .map(Variable::getRepresentation)
                .collect(Collectors.toList());
    }
}
