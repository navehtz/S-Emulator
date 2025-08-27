package program;

import exceptions.EngineLoadException;
import label.FixedLabel;
import label.Label;
import label.LabelImpl;
import loader.XmlProgramLoader;
import variable.Variable;
import variable.VariableImpl;
import variable.VariableType;
import instruction.Instruction;
import instruction.LabelReferencesInstruction;
import instruction.SyntheticInstruction;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ProgramImpl implements Program {

    private final String programName;
    private final List<Instruction> programInstructions;
    private final Set<Variable> inputVariables;
    private final Set<Variable> workVariables;
    private final Map<Label, Instruction> labelToInstruction;
    private final List<Label> labelsInProgram;  // Need it to keep the order of the labels
    private final Set<Label> labelsAddedAfterExtension;  // Need it to keep the order of the labels
    private final Set<Label> referencedLabels;

    private int nextLabelNumber = 1;
    private int nextWorkVariableNumber = 1;

    public ProgramImpl(String name) {
        this.programName = name;
        this.programInstructions = new ArrayList<>();
        this.labelToInstruction = new HashMap<>();
        this.inputVariables = new LinkedHashSet<>();
        this.workVariables = new LinkedHashSet<>();
        this.labelsInProgram = new ArrayList<>();
        this.labelsAddedAfterExtension = new LinkedHashSet<>();
        this.referencedLabels  = new LinkedHashSet<>();
    }

    // TODO:         this.labelsAddedAfterExtension = new LinkedHashSet<>();
    // TODO: understand the need , delete


    @Override
    public Program cloneProgram(Path xmlPath, int nextLabelNumber, int nextWorkVariableNumber) throws EngineLoadException {
        XmlProgramLoader loader = new XmlProgramLoader();
        Program cloned = loader.load(xmlPath);
        cloned.setNextLabelNumber(nextLabelNumber);
        cloned.setNextWorkVariableNumber(nextWorkVariableNumber);
        cloned.initialize();

        return cloned;
    }

    @Override
    public void setNextLabelNumber(int nextLabelNumber) {
        this.nextLabelNumber = nextLabelNumber;
    }

    @Override
    public void setNextWorkVariableNumber(int nextWorkVariableNumber) {
        this.nextWorkVariableNumber = nextWorkVariableNumber;
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

        //totalCycles += instruction.getCycleOfInstruction();
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
    public int getNextLabelNumber() {
        return nextLabelNumber;
    }

    @Override
    public int getNextWorkVariableNumber() {
        return nextWorkVariableNumber;
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

    private static final Comparator<Label> EXIT_LAST_THEN_NUMBER =
            Comparator.comparing((Label l) -> FixedLabel.EXIT.equals(l))
                    .thenComparingInt(Label::getNumber);

    @Override
    public List<String> getOrderedLabelsExitLastStr() {
        return java.util.stream.Stream
                .concat(labelsInProgram.stream(), referencedLabels.stream())
                .distinct()
                .sorted(EXIT_LAST_THEN_NUMBER)
                .map(Label::getLabelRepresentation)
                .toList();
    }

    @Override
    public List<String> getInputVariablesSortedStr() {
        return inputVariables.stream()
                .sorted(Comparator.comparingInt(Variable::getNumber))
                .map(Variable::getRepresentation)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> gerInstructionsAsStringList() {
        int n = programInstructions.size();
        return programInstructions.stream()
                .map(ins -> ins.getInstructionRepresentation(n))
                .collect(Collectors.toList());
    }


/*    private String programRepresentation() {
        StringBuilder programDisplay = new StringBuilder();
        int numberOfInstructionsInProgram = programInstructions.size();

        for (Instruction instruction : programInstructions) {
            String line = instruction.getInstructionRepresentation(numberOfInstructionsInProgram);
            programDisplay.append(line).append(System.lineSeparator());
        }

        return programDisplay.toString();
    }*/

/*    @Override
    public String getProgramDisplay() {

        List<String> variablesInputInProgram = getInputVariableSorted();
        List<String> labels = getOrderedLabelsExitLast();

        StringBuilder programDisplay = new StringBuilder();
        programDisplay.append("Name: ").append(getName()).append(System.lineSeparator());
        programDisplay.append("Inputs: ").append(String.join(", ", variablesInputInProgram)).append(System.lineSeparator());
        programDisplay.append("Labels: ").append(String.join(", ", labels)).append(System.lineSeparator());
        programDisplay.append("Instructions: ").append(System.lineSeparator());

        programDisplay.append(programRepresentation());

        return programDisplay.toString();
    }*/

/*    @Override
    public String getProgramDisplay() {
        List<String> variablesInputInProgram = getInputVariableSorted();
        List<String> labels = getOrderedLabelsExitLast();

        return String.format(
                "Name: %s%nInputs: %s%nLabels: %s%nInstructions:%n%s",
                getName(),
                String.join(", ", variablesInputInProgram),
                String.join(", ", labels),
                programRepresentation()
        );
    }*/

    @Override
    public List<List<String>> getExpandedProgram() {
        int numberOfInstructionsInProgram = programInstructions.size();
        List<List<String>> expandedProgram = new ArrayList<>();

        for (Instruction instruction : programInstructions) {
            List<String> chain = instruction.getInstructionExtendedDisplay(numberOfInstructionsInProgram);
            if (chain != null && !chain.isEmpty()) {
                expandedProgram.add(chain);
            }
        }

        return expandedProgram;
    }

 /*   @Override
    public String getExtendedProgramDisplay() {
        int numberOfInstructionsInProgram = programInstructions.size();

        String instructionsDisplay = programInstructions.stream()
                .map(instruction -> instruction.getInstructionExtendedDisplay(numberOfInstructionsInProgram))
                .collect(Collectors.joining(System.lineSeparator()));

        return String.format(
                "Name: %s%nInputs: %s%nLabels: %s%n%s",
                getName(),
                String.join(", ", getInputVariablesSortedStr()),
                String.join(", ", getOrderedLabelsExitLastStr()),
                instructionsDisplay
        );
    }*/

/*    @Override
    public String getExtendedProgramDisplay() {
        StringBuilder extendedProgramDisplay = new StringBuilder();

        int numberOfInstructionsInProgram = programInstructions.size();

        extendedProgramDisplay.append("Name: ").append(getName()).append(System.lineSeparator());
        extendedProgramDisplay.append("Inputs: ").append(String.join(", ", getInputVariableSorted())).append(System.lineSeparator());
        extendedProgramDisplay.append("Labels: ").append(String.join(", ", getOrderedLabelsExitLast())).append(System.lineSeparator());

        for(Instruction instruction : programInstructions) {
            extendedProgramDisplay.append(instruction.getInstructionExtendedDisplay(numberOfInstructionsInProgram)).append(System.lineSeparator());
        }

        return extendedProgramDisplay.toString();
    }*/

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

    @Override
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

                iterator.remove();                              // Remove the old instruction
                getLabelToInstruction().remove(originalLabel);       // Remove the label from the map because we will add it again in line 239
                getLabelsInProgram().remove(originalLabel);          // Remove the label from the map because we will add it again in line 239

                for (Instruction extendedInstruction : newInstructionsList) {
                    updateVariableAndLabel(extendedInstruction);
                    iterator.add(extendedInstruction);          // Add the extended (inner) instruction to the list
                }
            }
        }
    }

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

        if (labelsAddedAfterExtension.contains(uniqueLabel)) {
            throw new IllegalStateException(
                    "Attempted to add duplicate labels after extention: " + uniqueLabel.getLabelRepresentation()
            );
        }
        labelsAddedAfterExtension.add(uniqueLabel);
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

    @Override
    public List<Label> getLabelsInProgram() {
        return labelsInProgram;
    }

    @Override
    public Map<Label, Instruction> getLabelToInstruction() {
        return labelToInstruction;
    }

    public List<String> getProgramInstructionsAsString() {
        return programInstructions.stream()
                .map(Instruction::toString) // או מתודה אחרת, אם יש ייצוג מותאם
                .collect(Collectors.toList());
    }

    public Set<String> getInputVariablesAsString() {
        return inputVariables.stream()
                .map(Variable::toString) // או getName/getLabelRepresentation וכו'
                .collect(Collectors.toSet());
    }

    public Set<String> getWorkVariablesAsString() {
        return workVariables.stream()
                .map(Variable::toString)
                .collect(Collectors.toSet());
    }
}
