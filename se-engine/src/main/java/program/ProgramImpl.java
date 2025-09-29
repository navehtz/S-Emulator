package program;

import dto.InstructionDTO;
import exceptions.EngineLoadException;
import label.FixedLabel;
import label.Label;
import label.LabelImpl;
import operation.Operation;
import variable.Variable;
import variable.VariableImpl;
import variable.VariableType;
import instruction.Instruction;
import instruction.LabelReferencesInstruction;
import instruction.SyntheticInstruction;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public final class ProgramImpl extends Operation implements Program {

    private ProgramImpl(Builder b) {
        super(b);
        //Objects.requireNonNull(this.entry, "Program entry is required");
    }

    @Override
    public Label entry() {
        return entry;
    }

    public static final class Builder extends Operation.Builder<Builder, ProgramImpl> {

        @Override
        protected Builder self() {
            return this;
        }

        public Builder withEntry(Label entry) {
            this.entry = entry;
            return this;
        }

        @Override
        public ProgramImpl build() {
            return new ProgramImpl(this);
        }
    }
}

//public class ProgramImpl extends Operation implements Program, Serializable {
//
//    private final String programName;
//    private final List<Instruction> programInstructions;
//    private final Set<Variable> inputVariables;
//    private final Set<Variable> workVariables;
//    private final Map<Label, Instruction> labelToInstruction;
//    private final List<Label> labelsInProgram;              // keep order
//    private final Set<Label> labelsAddedAfterExtension;     // keep order after expansion
//    private final Set<Label> referencedLabels;
//
//    private int nextLabelNumber = 1;
//    private int nextWorkVariableNumber = 1;
//
//    // Private ctor for Builder
//    private ProgramImpl(Builder builder) {
//        this.programName = (builder.programName == null || builder.programName.isBlank())
//                ? "Unnamed Program" : builder.programName;
//
//        this.programInstructions = new ArrayList<>();
//        this.labelToInstruction = new LinkedHashMap<>();
//        this.inputVariables = new LinkedHashSet<>();
//        this.workVariables = new LinkedHashSet<>();
//        this.labelsInProgram = new ArrayList<>();
//        this.labelsAddedAfterExtension = new LinkedHashSet<>();
//        this.referencedLabels = new LinkedHashSet<>();
//
//        // bucket declared variables (even if not used in instructions)
//        for (Variable v : builder.variables) {
//            if (v == null) continue;
//            if (v.getType() == VariableType.INPUT)  inputVariables.add(v);
//            else if (v.getType() == VariableType.WORK) workVariables.add(v);
//        }
//
//        // keep declared labels in order
//        for (Label lbl : builder.labels) {
//            if (lbl != null && lbl != FixedLabel.EMPTY && !labelsInProgram.contains(lbl)) {
//                labelsInProgram.add(lbl);
//            }
//        }
//
//        // add instructions via instance addInstruction to keep maps/sets consistent
//        for (Instruction ins : builder.programInstructions) {
//            if (ins != null) addInstruction(ins);
//        }
//
//        sortVariableSetByNumber(inputVariables);
//        sortVariableSetByNumber(workVariables);
//        initialize();
//    }
//
//    // --------- Builder ----------
//    public static final class Builder {
//        private String programName;
//        private final List<Instruction> programInstructions = new ArrayList<>();
//        private final Set<Variable> variables = new LinkedHashSet<>();
//        private final List<Label> labels = new ArrayList<>();
//
//        public Builder withName(String name) {
//            this.programName = name;
//            return this;
//        }
//
//        public Builder withInstructions(Collection<? extends Instruction> newInstructions) {
//            programInstructions.clear();
//            if (newInstructions != null) programInstructions.addAll(newInstructions);
//            return this;
//        }
//
//        public Builder addInstruction(Instruction instruction) {
//            if (instruction != null) programInstructions.add(instruction);
//            return this;
//        }
//
//        public Builder addInstructions(Instruction... instructions) {
//            if (instructions != null) programInstructions.addAll(Arrays.asList(instructions));
//            return this;
//        }
//
//        public Builder withVariables(Collection<? extends Variable> newVariables) {
//            variables.clear();
//            if (newVariables != null) variables.addAll(newVariables);
//            return this;
//        }
//
//        public Builder addVariable(Variable v) {
//            if (v != null) variables.add(v);
//            return this;
//        }
//
//        public Builder addVariables(Variable... vars) {
//            if (vars != null) variables.addAll(Arrays.asList(vars));
//            return this;
//        }
//
//        public Builder withLabels(Collection<? extends Label> newLabels) {
//            labels.clear();
//            if (newLabels != null) labels.addAll(newLabels);
//            return this;
//        }
//
//        public Builder addLabel(Label l) {
//            if (l != null) labels.add(l);
//            return this;
//        }
//
//        public Builder addLabels(Label... ls) {
//            if (ls != null) labels.addAll(Arrays.asList(ls));
//            return this;
//        }
//
//        public ProgramImpl build() {
//            return new ProgramImpl(this);
//        }
//    }
//
//    // --------- cloning ----------
//    @Override
//    public ProgramImpl deepClone() {
//        try {
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
//                out.writeObject(this);
//            }
//            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
//            try (ObjectInputStream in = new ObjectInputStream(bis)) {
//                return (ProgramImpl) in.readObject();
//            }
//        } catch (IOException | ClassNotFoundException e) {
//            throw new RuntimeException("Failed cloning program", e);
//        }
//    }
//
//    @Override
//    public void initialize() {
//        initNextLabelNumber();
//        initNextWorkVariableNumber();
//    }
//
//    // --------- Program API ----------
//    @Override
//    public String getName() {
//        return this.programName;
//    }
//
//    @Override
//    public void addInstruction(Instruction instruction) {
//        updateVariableAndLabel(instruction);
//        programInstructions.add(instruction);
//    }
//
//    private void updateVariableAndLabel(Instruction instruction) {
//        if (instruction == null) return;
//        Label currentLabel = instruction.getLabel();
//
//        bucketLabel(instruction, currentLabel);
//        bucketVariable(instruction.getTargetVariable());
//        bucketVariable(instruction.getSourceVariable());
//    }
//
//    private void bucketLabel(Instruction instruction, Label currentLabel) {
//        if (currentLabel != FixedLabel.EMPTY) {
//            if (!labelToInstruction.containsKey(currentLabel)) {
//                labelsInProgram.add(currentLabel);
//                labelToInstruction.put(currentLabel, instruction);
//            } else {
//                throw new IllegalArgumentException(
//                        "Duplicate label " + currentLabel.getLabelRepresentation() +
//                                " at instructions: " +
//                                labelToInstruction.get(currentLabel).getName() +
//                                " and " + instruction.getName()
//                );
//            }
//        }
//
//        if (instruction instanceof LabelReferencesInstruction labelReferencesInstruction) {
//            Label addedLabel = labelReferencesInstruction.getReferenceLabel();
//            referencedLabels.add(addedLabel);
//        }
//    }
//
//    private void bucketVariable(Variable variable) {
//        if (variable == null) return;
//        switch (variable.getType()) {
//            case INPUT -> inputVariables.add(variable);
//            case WORK  -> workVariables.add(variable);
//            default -> {}
//        }
//    }
//
//    @Override
//    public List<Instruction> getInstructionsList() {
//        return this.programInstructions;
//    }
//
//    @Override
//    public Instruction getInstructionByLabel(Label label) {
//        return labelToInstruction.get(label);
//    }
//
//    @Override
//    public Set<Variable> getInputVariables() {
//        return this.inputVariables;
//    }
//
//    @Override
//    public Set<Variable> getWorkVariables() {
//        return this.workVariables;
//    }
//
//    @Override
//    public void validateProgram() throws EngineLoadException {
//        validateLabelReferencesExist();
//    }
//
//    private void validateLabelReferencesExist() throws EngineLoadException {
//        Set<Label> undefined = new LinkedHashSet<>(referencedLabels);
//        undefined.removeAll(labelToInstruction.keySet());
//        undefined.remove(FixedLabel.EXIT);
//
//        if (!undefined.isEmpty()) {
//            String names = undefined.stream()
//                    .filter(lbl -> lbl != FixedLabel.EMPTY && lbl != FixedLabel.EXIT)
//                    .map(Label::getLabelRepresentation)
//                    .collect(Collectors.joining(", "));
//            throw new EngineLoadException("Undefined label reference(s) in the program: " + names);
//        }
//    }
//
//    private static final Comparator<Label> EXIT_LAST_THEN_NUMBER =
//            Comparator.comparing((Label l) -> FixedLabel.EXIT.equals(l))
//                    .thenComparingInt(Label::getIndex);
//
//    @Override
//    public List<String> getOrderedLabelsExitLastStr() {
//        return java.util.stream.Stream
//                .concat(labelsInProgram.stream(), referencedLabels.stream())
//                .distinct()
//                .sorted(EXIT_LAST_THEN_NUMBER)
//                .map(Label::getLabelRepresentation)
//                .toList();
//    }
//
//    @Override
//    public List<String> getInputVariablesSortedStr() {
//        return inputVariables.stream()
//                .sorted(Comparator.comparingInt(Variable::getIndex))
//                .map(Variable::getRepresentation)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<InstructionDTO> getInstructionDTOList() {
//        List<InstructionDTO> instructionDTOList = new ArrayList<>();
//        for (Instruction currInstruction : programInstructions) {
//            instructionDTOList.add(currInstruction.getInstructionDTO());
//        }
//        return instructionDTOList;
//    }
//
//    @Override
//    public List<List<InstructionDTO>> getExpandedProgram() {
//        List<List<InstructionDTO>> expandedProgram = new ArrayList<>();
//        for (Instruction instruction : programInstructions) {
//            List<InstructionDTO> chain = instruction.getInstructionExtendedList();
//            if (chain != null && !chain.isEmpty()) expandedProgram.add(chain);
//        }
//        return expandedProgram;
//    }
//
//    @Override
//    public List<Label> getLabelsInProgram() {
//        return labelsInProgram;
//    }
//
//    @Override
//    public Map<Label, Instruction> getLabelToInstruction() {
//        return labelToInstruction;
//    }
//
//    @Override
//    public int calculateProgramMaxDegree() {
//        int maxDegree = 0;
//
//        for (Instruction instruction : programInstructions) {
//            if (instruction instanceof SyntheticInstruction syntheticInstruction) {
//                maxDegree = Math.max(maxDegree, syntheticInstruction.getMaxDegree());
//            }
//        }
//
//        return maxDegree;
//    }
//
//    @Override
//    public void expandProgram(int degree) {
//        for (int i = 0 ; i < degree ; i++) {
//            int nextInstructionNumber = 1;
//
//            for (ListIterator<Instruction> iterator = getInstructionsList().listIterator(); iterator.hasNext(); ) {
//                Instruction instruction = iterator.next();
//                Label originalLabel = instruction.getLabel();
//                List<Instruction> newInstructionsList = new ArrayList<>();
//
//                // initialize
//                instruction.setProgramOfThisInstruction(this);
//                if (instruction instanceof SyntheticInstruction syntheticInstruction) {
//                    nextInstructionNumber = syntheticInstruction.setInnerInstructionsAndReturnTheNextOne(nextInstructionNumber);
//                    newInstructionsList = instruction.getExtendedInstruction();
//                }
//                else {
//                    Instruction cloneInstruction = instruction.createInstructionWithInstructionNumber(nextInstructionNumber);
//                    newInstructionsList.add(cloneInstruction);
//                    nextInstructionNumber++;
//                }
//
//                iterator.remove();
//                getLabelToInstruction().remove(originalLabel);
//                getLabelsInProgram().remove(originalLabel);
//
//                for (Instruction extendedInstruction : newInstructionsList) {
//                    updateVariableAndLabel(extendedInstruction);
//                    iterator.add(extendedInstruction);
//                }
//            }
//        }
//    }
//
//    private void initNextLabelNumber() {
//        nextLabelNumber = labelsInProgram.stream()
//                .map(Label::getLabelRepresentation)
//                .filter(s -> s.matches("L\\d+"))
//                .mapToInt(s -> Integer.parseInt(s.substring(1)))
//                .max().orElse(0) + 1;
//    }
//
//    @Override
//    public Label generateUniqueLabel() {
//        Label uniqueLabel = new LabelImpl(nextLabelNumber++);
//        if (labelsAddedAfterExtension.contains(uniqueLabel)) {
//            throw new IllegalStateException(
//                    "Attempted to add duplicate labels after extension: " + uniqueLabel.getLabelRepresentation()
//            );
//        }
//        labelsAddedAfterExtension.add(uniqueLabel);
//        return uniqueLabel;
//    }
//
//    private void initNextWorkVariableNumber() {
//        nextWorkVariableNumber = workVariables.stream()
//                .filter(v -> v.getType() == VariableType.WORK)
//                .map(Variable::getRepresentation)
//                .map(rep -> {
//                    String digits = rep.replaceAll("\\D+", "");    // Only the digits
//                    return digits.isEmpty() ? 0 : Integer.parseInt(digits);
//                })
//                .max(Integer::compare)
//                .orElse(0) + 1;                                   // Return 1 if the set is empty
//    }
//
//    @Override
//    public Variable generateUniqueVariable() {
//        Variable v = new VariableImpl(VariableType.WORK, nextWorkVariableNumber++);
//        workVariables.add(v);
//        return v;
//    }
//
//    @Override
//    public void sortVariableSetByNumber(Set<Variable> variablesSet) {
//        var sorted = variablesSet.stream()
//                .sorted(Comparator.comparingInt(Variable::getIndex))
//                .toList();
//
//        variablesSet.clear();
//        variablesSet.addAll(sorted);
//    }
//
//    @Override
//    public void addInputVariable(Variable variable) {
//        if (variable != null) inputVariables.add(variable);
//    }
//
//    @Override
//    public List<Variable> getInputAndWorkVariablesSortedBySerial() {
//        sortVariableSetByNumber(inputVariables);
//        sortVariableSetByNumber(workVariables);
//
//        List<Variable> inputAndWorkVariablesAndTheirValues = new ArrayList<>(inputVariables);
//        inputAndWorkVariablesAndTheirValues.addAll(workVariables);
//        return inputAndWorkVariablesAndTheirValues;
//    }
//}
