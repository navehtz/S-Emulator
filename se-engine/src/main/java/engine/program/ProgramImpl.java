package engine.program;

import engine.exceptions.EngineLoadException;
import engine.instruction.Instruction;
import engine.instruction.LabelReferencesInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProgramImpl implements Program {

    private final String programName;
    private final List<Instruction> programInstructions;
    private final Set<Variable> inputVariables;
    private final Set<Variable> workVariables;
    private final Map<Label, Instruction> labelToInstruction;
    private final List<Label> labelsInProgram;  // Need it to keep the order of the labels
    private final Set<Label> referencedLabels ;

    public ProgramImpl(String name) {
        this.programName = name;
        this.programInstructions = new ArrayList<>();
        this.labelToInstruction = new HashMap<>();
        this.inputVariables = new LinkedHashSet<>();
        this.workVariables = new LinkedHashSet<>();
        this.labelsInProgram = new ArrayList<>();
        this.referencedLabels  = new LinkedHashSet<>();
    }

    @Override
    public String getName() {
        return this.programName;
    }

    @Override
    public void addInstruction(Instruction instruction) {

        Label currentLabel = instruction.getLabel();
        if(currentLabel != FixedLabel.EMPTY) {                    // Add label and its instruction to map
            if (!labelToInstruction.containsKey(currentLabel)) {
                labelsInProgram.add(currentLabel);
                labelToInstruction.put(currentLabel, instruction);
            } else {
                throw new IllegalArgumentException(
                        "Duplicate label " + currentLabel.getLabelRepresentation() + " at instructions: " +
                                labelToInstruction.get(currentLabel).getName() + " and " + instruction.getName()
                );
            }
        }

        if (instruction instanceof LabelReferencesInstruction labelReferencesInstruction) {
            Label addedLabel = labelReferencesInstruction.getReferenceLabel();
            referencedLabels .add(addedLabel);
        }

        bucketVariable(instruction.getTargetVariable());
        bucketVariable(instruction.getSourceVariable());
        
        programInstructions.add(instruction);
    }

    private void bucketVariable(Variable variable) {
        if (variable == null) return;

        switch (variable.getType()) {
            case INPUT -> inputVariables.add(variable);
            case WORK  -> workVariables.add(variable);
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
        return labelToInstruction.get(label);
    }

    @Override
    public Set<Variable> getInputVariables() {
        return this.inputVariables;
    }

    @Override
    public Set<Variable> getWorkVariables() {
        return this.workVariables;
    }

    @Override
    public String getProgramDisplay() {

        List<String> inputs = displayInputVariable();
        List<String> labels = displayOrderedLabelsExitLast();

        StringBuilder programDisplay = new StringBuilder();
        programDisplay.append("Program: ").append(getName()).append(System.lineSeparator());
        programDisplay.append("Inputs: ").append(String.join(", ", inputs)).append(System.lineSeparator());
        programDisplay.append("Labels: ").append(String.join(", ", labels)).append(System.lineSeparator());
        programDisplay.append("Instructions: ").append(System.lineSeparator());

        programDisplay.append(programRepresentation());

        return programDisplay.toString();
    }

    @Override
    public void validateProgram() throws EngineLoadException {
        validateLabelReferencesExist();
    }

    private void validateLabelReferencesExist() throws EngineLoadException {
        Set<Label> undefined = new java.util.LinkedHashSet<>(referencedLabels);
        undefined.removeAll(labelToInstruction.keySet());
        undefined.remove(FixedLabel.EXIT);

        if (!undefined.isEmpty()) {
            String names = undefined.stream()
                    .filter(lbl -> lbl != FixedLabel.EMPTY && lbl != FixedLabel.EXIT)
                    .map(Label::getLabelRepresentation)
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new EngineLoadException("Undefined label reference(s) in the program: " + names);
        }
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

    private String programRepresentation() {
        StringBuilder programDisplay = new StringBuilder();

        for(int i = 0; i < programInstructions.size(); i++) {
            Instruction instruction = programInstructions.get(i);
            String line = instruction.instructionRepresentation(i + 1);
            programDisplay.append(line).append(System.lineSeparator());
        }

        return programDisplay.toString();
    }
}
