package program;

import exceptions.EngineLoadException;
import label.FixedLabel;
import label.Label;
import label.LabelImpl;
import variable.Variable;
import variable.VariableImpl;
import variable.VariableType;
import instruction.Instruction;
import instruction.LabelReferencesInstruction;
import instruction.SyntheticInstruction;

import java.util.*;
import java.util.stream.Collectors;

public class ProgramImpl implements Program {

    private final String programName;
    private final Set<Variable> inputVariables;
    private final Set<Variable> workVariables;
    private final Set<Label> labelsInProgram;                  // Need it to keep the order of the labels
    private final Set<Label> referencedLabels ;

    private final List<Instruction> programInstructions;
    private final Map<Label, Instruction> labelToInstruction;
    private final Set<Label> labelsAddedAfterExtension;

    private int nextLabelNumber = 1;
    private int nextWorkVariableNumber = 1;


    // TODO: make sure i dont need it !!!!!!
    //private final Set<Label> labelsAddedAfterExtension;  // Need it to keep the order of the labels


    public ProgramImpl(String name) {
        this.programName = name;
        this.programInstructions = new ArrayList<>();
        this.inputVariables = new LinkedHashSet<>();
        this.workVariables = new LinkedHashSet<>();
        this.labelsInProgram = new LinkedHashSet<>();
        this.labelToInstruction = new HashMap<>();
        this.referencedLabels  = new LinkedHashSet<>();
        this.labelsAddedAfterExtension = new LinkedHashSet<>();
    }

    @Override
    public void initialize() {
        initNextLabelNumber();
        initNextWorkVariableNumber();
    }

    @Override
    public void initializeByOtherProgram(Program originalProgram) {
        this.labelsInProgram.addAll(originalProgram.getLabelsInProgram());
        this.referencedLabels.addAll(originalProgram.getReferencedLabels());
        this.inputVariables.addAll(originalProgram.getInputVariables());
        this.workVariables.addAll(originalProgram.getWorkVariables());
        this.setNextLabelNumber(originalProgram.getNextLabelNumber());
        this.setNextWorkVariableNumber(originalProgram.getNextWorkVariableNumber());
    }

    @Override
    public String getName() {
        return this.programName;
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

        List<String> variablesInputInProgram = getInputVariableSorted();
        List<String> labels = getOrderedLabelsExitLast();

        StringBuilder programDisplay = new StringBuilder();
        programDisplay.append("Program Display:").append(System.lineSeparator());
        programDisplay.append("Name: ").append(getName()).append(System.lineSeparator());
        programDisplay.append("Inputs: ").append(String.join(", ", variablesInputInProgram)).append(System.lineSeparator());
        programDisplay.append("Labels: ").append(String.join(", ", labels)).append(System.lineSeparator());
        programDisplay.append("Instructions: ").append(System.lineSeparator());

        programDisplay.append(programRepresentation());

        return programDisplay.toString();
    }

    @Override
    public Set<Label> getLabelsAddedAfterExtension() {
        return this.labelsAddedAfterExtension;
    }

    private List<String> getOrderedLabelsExitLast() {
        return labelsInProgram.stream()
                .sorted(
                        Comparator.comparing((Label l) -> FixedLabel.EXIT.equals(l))
                                .thenComparingInt(Label::getNumber)
                )
                .map(Label::getLabelRepresentation)
                .toList();
    }

    @Override
    public List<String> getInputVariableSorted() {
        return inputVariables.stream()
                .sorted(Comparator.comparingInt(Variable::getNumber))
                .map(Variable::getRepresentation)
                .collect(Collectors.toList());
    }

    @Override
    public String getExtendedProgramDisplay() {
        StringBuilder extendedProgramDisplay = new StringBuilder();

        int numberOfInstructionsInProgram = programInstructions.size();

        for(Instruction instruction : programInstructions) {
            extendedProgramDisplay.append(instruction.getInstructionExtendedDisplay(numberOfInstructionsInProgram)).append(System.lineSeparator());
        }

        return extendedProgramDisplay.toString();
    }

    @Override
    public void setNextLabelNumber(int nextLabelNumber) {
        this.nextLabelNumber = nextLabelNumber;
    }

    @Override
    public int getNextLabelNumber() {
        return this.nextLabelNumber;
    }

    @Override
    public void setNextWorkVariableNumber(int nextWorkVariableNumber) {
        this.nextWorkVariableNumber = nextWorkVariableNumber;
    }

    @Override
    public int getNextWorkVariableNumber() {
        return this.nextWorkVariableNumber;
    }

    @Override
    public Set<Label> getLabelsInProgram() {
        return labelsInProgram;
    }

    @Override
    public Map<Label, Instruction> getLabelToInstruction() {
        return labelToInstruction;
    }

    @Override
    public Set<Label> getReferencedLabels() {
        return referencedLabels;
    }


    private String programRepresentation() {
        StringBuilder programDisplay = new StringBuilder();
        int numberOfInstructionsInProgram = programInstructions.size();

        for (Instruction instruction : programInstructions) {
            String line = instruction.getInstructionRepresentation(numberOfInstructionsInProgram);
            programDisplay.append(line).append(System.lineSeparator());
        }

        return programDisplay.toString();
    }

    @Override
    public int calculateProgramMaxDegree() {
        int maxDegree = 0;

        for (Instruction instruction : programInstructions) {
            if(instruction instanceof SyntheticInstruction syntheticInstruction) {
                maxDegree = Math.max(maxDegree, syntheticInstruction.getMaxDegree());
            }
        }

        return maxDegree;
    }

    // Create a new program from an old one.
/*    @Override
    public Program expandProgram(int degree) {
        if (degree == 0) {
            return this;
        }

        Program expandedProgram = new ProgramImpl(this.programName);
        expandedProgram.initializeByOtherProgram(this);

        for (int i = 0 ; i < degree ; i++) {
            int nextInstructionNumber = 1;

            for (ListIterator<Instruction> iterator = this.programInstructions.listIterator(); iterator.hasNext(); ) {
                Instruction instruction = iterator.next();
                Label originalLabel = instruction.getLabel();
                List<Instruction> newInstructionsList = new ArrayList<>();

                // initialize
                instruction.setProgramOfThisInstruction(expandedProgram);
                if (instruction instanceof SyntheticInstruction syntheticInstruction) {
                    nextInstructionNumber = syntheticInstruction.setInnerInstructionsAndReturnTheNextOne(nextInstructionNumber);
                    newInstructionsList = instruction.getExtendedInstruction();
                }
                else {
                    Instruction cloneInstruction = instruction.createInstructionWithInstructionNumber(nextInstructionNumber);
                    newInstructionsList.add(cloneInstruction);
                    nextInstructionNumber++;
                }

                for (Instruction newInstruction : newInstructionsList) {
                    expandedProgram.addInstruction(newInstruction);
                }

            }
        }

        return expandedProgram;
    }*/


    @Override
    public Program expandProgram(int degree) {
        Program result = new ProgramImpl(this.programName);
        Program expandByOneDegree;

        for (int i = 0 ; i < degree ; i++) {

            if (i == 0) {
                expandByOneDegree = new ProgramImpl(this.programName);
                expandByOneDegree.initializeByOtherProgram(this);
                result = expandByOneDegree.expandByOneDegree(this);
            } else {
                expandByOneDegree = new ProgramImpl(result.getName());
                expandByOneDegree.initializeByOtherProgram(result);
                result = expandByOneDegree.expandByOneDegree(result);
            }
        }

        return result;
    }

    @Override
    public Program expandByOneDegree(Program originalProgram) {
        int nextInstructionNumber = 1;

        for (ListIterator<Instruction> iterator = originalProgram.getInstructionsList().listIterator(); iterator.hasNext(); ) {
            Instruction instruction = iterator.next();
            Label originalLabel = instruction.getLabel();
            List<Instruction> newInstructionsList = new ArrayList<>();

            // initialize
            instruction.setProgramOfThisInstruction(this);
            if (instruction instanceof SyntheticInstruction syntheticInstruction) {
                nextInstructionNumber = syntheticInstruction.setInnerInstructionsAndReturnTheNextOne(nextInstructionNumber);
                newInstructionsList = instruction.getExtendedInstruction();
            }
            else {
                Instruction cloneInstruction = instruction.createInstructionWithInstructionNumber(nextInstructionNumber);
                newInstructionsList.add(cloneInstruction);
                nextInstructionNumber++;
            }

            for (Instruction newInstruction : newInstructionsList) {
                this.addInstruction(newInstruction);
            }
        }

        return this;
    }

/*    @Override
    public void expandProgram(int degree) {

        for (int i = 0 ; i < degree ; i++) {
            int nextInstructionNumber = 1;

            for (ListIterator<Instruction> iterator = getInstructionsList().listIterator(); iterator.hasNext(); ) {
                Instruction instruction = iterator.next();
                Label originalLabel = instruction.getLabel();
                List<Instruction> newInstructionsList = new ArrayList<>();

                // initialize
                instruction.setProgramOfThisInstruction(this);
                if (instruction instanceof SyntheticInstruction syntheticInstruction) {
                    nextInstructionNumber = syntheticInstruction.setInnerInstructionsAndReturnTheNextOne(nextInstructionNumber);
                    newInstructionsList = instruction.getExtendedInstruction();
                }
                else {
                    Instruction cloneInstruction = instruction.createInstructionWithInstructionNumber(nextInstructionNumber);
                    newInstructionsList.add(cloneInstruction);
                    nextInstructionNumber++;
                }

                iterator.remove();                                    // Remove the old instruction
                getLabelToInstruction().remove(originalLabel);       // Remove the label from the map because we will add it again in line 239
                getLabelsInProgram().remove(originalLabel);          // Remove the label from the map because we will add it again in line 239

                for (Instruction extendedInstruction : newInstructionsList) {
                    updateVariableAndLabel(extendedInstruction);
                    iterator.add(extendedInstruction);          // Add the extended (inner) instruction to the list
                }
            }
        }
    }*/

    @Override
    public void addInstruction(Instruction instruction) {

        updateVariableAndLabel(instruction);
        this.getInstructionsList().add(instruction);
    }

    private void updateVariableAndLabel(Instruction instruction) {
        Label currentLabel = instruction.getLabel();

        bucketLabel(instruction, currentLabel);
        bucketVariable(instruction.getTargetVariable());
        bucketVariable(instruction.getSourceVariable());
    }

    private void bucketLabel(Instruction instruction, Label currentLabel) {
        if(currentLabel != FixedLabel.EMPTY) {                       // Add label and its instruction to map of program
            if (!this.getLabelToInstruction().containsKey(currentLabel)) {
                this.getLabelsInProgram().add(currentLabel);
                this.getLabelToInstruction().put(currentLabel, instruction);
            } else {
                throw new IllegalArgumentException(
                        "Duplicate label " + currentLabel.getLabelRepresentation() + " at instructions: " +
                                this.getLabelToInstruction().get(currentLabel).getName() + " and " + instruction.getName()
                );
            }
        }

        if (instruction instanceof LabelReferencesInstruction labelReferencesInstruction) {
            Label addedLabel = labelReferencesInstruction.getReferenceLabel();
            this.getReferencedLabels().add(addedLabel);
        }
    }

    private void bucketVariable(Variable variable) {
        if (variable == null) return;

        switch (variable.getType()) {
            case INPUT -> this.getInputVariables().add(variable);
            case WORK  -> this.getWorkVariables().add(variable);
        }
    }

/*    private void bucketLabel(Instruction instruction, Label currentLabel) {
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
    }*/

    @Override
    public void validateProgram() throws EngineLoadException {
        validateLabelReferencesExist();
    }

    private void validateLabelReferencesExist() throws EngineLoadException {
        Set<Label> undefined = new java.util.LinkedHashSet<>(this.getReferencedLabels());
        undefined.removeAll(this.getLabelToInstruction().keySet());
        undefined.remove(FixedLabel.EXIT);

        if (!undefined.isEmpty()) {
            String names = undefined.stream()
                    .filter(lbl -> lbl != FixedLabel.EMPTY && lbl != FixedLabel.EXIT)
                    .map(Label::getLabelRepresentation)
                    .collect(java.util.stream.Collectors.joining(", "));
            throw new EngineLoadException("Undefined label reference(s) in the program " + this.getName() + ": " + names);
        }
    }

    private void initNextWorkVariableNumber() {
        int nextWorkVariableNumber = this.getWorkVariables()
                .stream()
                .filter(v -> v.getType() == VariableType.WORK)
                .map(Variable::getRepresentation)
                .map(rep -> {
                    String digits = rep.replaceAll("\\D+", "");    // Only the digits
                    return digits.isEmpty() ? 0 : Integer.parseInt(digits);
                })
                .max(Integer::compare)
                .orElse(0) + 1;                                   // Return 1 if the set is empty

        this.setNextWorkVariableNumber(nextWorkVariableNumber);
    }

    private void initNextLabelNumber() {
        int nextLabelNumber = this.getLabelsInProgram()
                .stream()
                .map(Label::getLabelRepresentation)
                .filter(s -> s.matches("L\\d+"))
                .mapToInt(s -> Integer.parseInt(s.substring(1)))
                .max().orElse(0) + 1;

        this.setNextLabelNumber(nextLabelNumber);
    }

    @Override
    public Label generateUniqueLabelAndUpdateNextLabelNumber() {
        int nextLabelNumber = this.getNextLabelNumber();
        Label uniqueLabel = new LabelImpl(nextLabelNumber);

        if (this.getLabelsAddedAfterExtension().contains(uniqueLabel)) {
            throw new IllegalStateException(
                    "Attempted to add duplicate labels after extention: " + uniqueLabel.getLabelRepresentation()
            );
        }

        this.getLabelsAddedAfterExtension().add(uniqueLabel);
        this.setNextLabelNumber(++nextLabelNumber);
        return uniqueLabel;
    }

    @Override
    public Variable generateUniqueVariableAndUpdateNextVariableNumber() {
        int nextWorkVariableNumber = this.getNextWorkVariableNumber();
        Variable newWorkVariable = new VariableImpl(VariableType.WORK, nextWorkVariableNumber);
        this.getWorkVariables().add(newWorkVariable);

        this.setNextWorkVariableNumber(++nextWorkVariableNumber);

        return newWorkVariable;
    }

    @Override
    public void sortVariableSetByNumber(Set<Variable> variablesSet) {
        var sorted = variablesSet.stream()
                .sorted(Comparator.comparingInt(Variable::getNumber))
                .toList();

        variablesSet.clear();
        variablesSet.addAll(sorted);
    }

    @Override
    public void addInputVariable(Variable variable) {
        inputVariables.add(variable);
    }

    @Override
    public List<Variable> getInputAndWorkVariablesSortedBySerial() {
        sortVariableSetByNumber(inputVariables);
        sortVariableSetByNumber(workVariables);

        List<Variable> inputAndWorkVariablesAndTheirValues = new ArrayList<>(inputVariables);
        inputAndWorkVariablesAndTheirValues.addAll(workVariables);
        return inputAndWorkVariablesAndTheirValues;
    }
}
