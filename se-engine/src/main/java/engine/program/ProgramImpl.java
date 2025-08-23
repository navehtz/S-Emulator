package engine.program;

import engine.exceptions.EngineLoadException;
import engine.instruction.*;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.label.LabelImpl;
import engine.variable.Variable;
import engine.variable.VariableImpl;
import engine.variable.VariableType;

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
    private final Set<Label> labelsAddedAfterExtention;  // Need it to keep the order of the labels
    private final Set<Label> referencedLabels ;
    private int totalCycles = 0;
    private int nextLabelNumber = 1;
    private int nextWorkVariableNumber = 1;

    public ProgramImpl(String name) {
        this.programName = name;
        this.programInstructions = new ArrayList<>();
        this.labelToInstruction = new HashMap<>();
        this.inputVariables = new LinkedHashSet<>();
        this.workVariables = new LinkedHashSet<>();
        this.labelsInProgram = new ArrayList<>();
        this.labelsAddedAfterExtention = new LinkedHashSet<>();
        this.referencedLabels  = new LinkedHashSet<>();
    }

    @Override
    public void initialize() {
        initNextLabelNumber();
        initNextWorkVariableNumber();
    }

    @Override
    public String getName() {
        return this.programName;
    }

    @Override
    public void addInstruction(Instruction instruction) {

        updateVariableAndLabel(instruction);

        totalCycles += instruction.getCycleOfInstruction();
        programInstructions.add(instruction);
    }

    private void updateVariableAndLabel(Instruction instruction) {
        Label currentLabel = instruction.getLabel();

        bucketLabel(instruction, currentLabel);
        bucketVariable(instruction.getTargetVariable());
        bucketVariable(instruction.getSourceVariable());
    }

    private void bucketLabel(Instruction instruction, Label currentLabel) {
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
            referencedLabels.add(addedLabel);
        }
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
        programDisplay.append("Labels: ").append(String.join(", ", labels)).append(System.lineSeparator()).append(System.lineSeparator());
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
                .sorted(Comparator.comparingInt(Variable::getNumber))
                .map(Variable::getRepresentation)
                .collect(Collectors.toList());
    }

    private String programRepresentation() {
        StringBuilder programDisplay = new StringBuilder();
        int numberOfInstructionsInProgram = programInstructions.size();

        for(int i = 0; i < programInstructions.size(); i++) {
            Instruction instruction = programInstructions.get(i);
            String line = instruction.instructionRepresentation(numberOfInstructionsInProgram, i + 1);
            programDisplay.append(line).append(System.lineSeparator());
        }

        return programDisplay.toString();
    }

    @Override
    public int getTotalCyclesOfProgram() {
        return totalCycles;
    }

    @Override
    public int calculateProgramMaxDegree() {
        int maxDegree = 0;

        for (Instruction instruction : programInstructions) {
            maxDegree = Math.max(maxDegree, instruction.calculateInstructionMaxDegree(this));
        }

        return maxDegree;
    }

    @Override
    public void extendProgram(int degree) {
        if (degree <= 0) {
            return;
        }

        for (int i = 0 ; i < degree ; i++) {
            for (ListIterator<Instruction> iterator = programInstructions.listIterator(); iterator.hasNext(); ) {
                Instruction instruction = iterator.next();
                Label originalLabel = instruction.getLabel();
                List<Instruction> extendedInstructions = instruction.getExtendedInstruction();

                if(extendedInstructions.size() == 1 && extendedInstructions.getFirst() == instruction) {   // If instruction is basic
                    continue;
                }

                iterator.remove();                              // Remove the old (synthetic) instruction
                labelToInstruction.remove(originalLabel);       // Remove the label from the map because we will add it again in line 239
                labelsInProgram.remove(originalLabel);          // Remove the label from the map because we will add it again in line 239

                for (Instruction extendedInstruction : extendedInstructions) {
                    updateVariableAndLabel(extendedInstruction);
                    iterator.add(extendedInstruction);        // Add the extended (inner) instruction to the list

/*                    if (extendedInstruction instanceof SyntheticInstruction syntheticInstruction) {
                        extendedInstruction.setProgramOfThisInstruction(this);
                        syntheticInstruction.setInnerInstructions();
                    }*/
                }
            }
        }
    }

/*    @Override
    public void extendProgram(int degree, ExecutionContext context) {
        if (degree <= 0) {
            return;
        }
        boolean firstIter = true;

        for (ListIterator<Instruction> iterator = programInstructions.listIterator(); iterator.hasNext(); ) {
            Instruction instruction = iterator.next();
            Label originalLabel = instruction.getLabel();
            List<Instruction> extendedInstructions = instruction.getExtendedInstruction(degree, context, this);


            if(extendedInstructions.size() == 1 && extendedInstructions.get(0) == instruction) {   // If instruction is basic
                continue;
            }

            if (firstIter) {
                if (originalLabel != FixedLabel.EMPTY) {
                    Instruction firstExtendedInstruction = extendedInstructions.remove(0);
                    labelToInstruction.remove(originalLabel);
                    labelsInProgram.remove(originalLabel);
                    extendedInstructions.addFirst(firstExtendedInstruction.createNewInstructionWithNewLabel(originalLabel));
                }

                firstIter = false;
            }

            iterator.remove();                             // Remove the old (synthetic) instruction
            for (Instruction extendedInstruction : extendedInstructions) {
                updateVariableAndLabel(extendedInstruction);
                iterator.add(extendedInstruction);        // Add the extended (inner) instruction to the list
            }
        }
    }*/

    private void initNextLabelNumber() {
        nextLabelNumber = labelsInProgram.stream()
                .map(Label::getLabelRepresentation)
                .filter(s -> s.matches("L\\d+"))
                .mapToInt(s -> Integer.parseInt(s.substring(1)))
                .max().orElse(0) + 1;
    }

    @Override
    public Label generateUniqueLabel() {
        Label uniqueLabel = new LabelImpl(nextLabelNumber++);

        if (labelsAddedAfterExtention.contains(uniqueLabel)) {
            throw new IllegalStateException(
                    "Attempted to add duplicate labels after extention: " + uniqueLabel.getLabelRepresentation()
            );
        }
        labelsAddedAfterExtention.add(uniqueLabel);
        return uniqueLabel;
    }

    private void initNextWorkVariableNumber() {
        nextWorkVariableNumber = workVariables.stream()
                .filter(v -> v.getType() == VariableType.WORK)
                .map(Variable::getRepresentation)
                .map(rep -> {
                    String digits = rep.replaceAll("\\D+", "");    // Only the digits
                    return digits.isEmpty() ? 0 : Integer.parseInt(digits);
                })
                .max(Integer::compare)
                .orElse(0) + 1;                                   // Return 1 if the set is empty
    }

    @Override
    public Variable generateUniqueVariable() {
        Variable v = new VariableImpl(VariableType.WORK, nextWorkVariableNumber++);
        workVariables.add(v);
        return v;
    }

    @Override
    public void sortInputVariablesByTypeThenNumber() {
        var sorted = inputVariables.stream()
                .sorted(Comparator.comparingInt(Variable::getNumber))
                .toList();

        inputVariables.clear();
        inputVariables.addAll(sorted);
    }

    @Override
    public void addInputVariable(Variable variable) {
        inputVariables.add(variable);
    }
}
