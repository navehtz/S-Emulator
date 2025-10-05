package operation;

import dto.InstructionDTO;
import engine.ProgramRegistry;
import exceptions.EngineLoadException;
import function.FunctionDisplayResolver;
import instruction.Instruction;
import instruction.LabelReferencesInstruction;
import instruction.SyntheticInstruction;
import instruction.synthetic.QuoteInstruction;
import instruction.SourceVariableInstruction;
import label.FixedLabel;
import label.Label;
import label.LabelImpl;
import program.Program;
import variable.Variable;
import variable.VariableImpl;
import variable.VariableType;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public abstract class Operation implements OperationView, Serializable {

    protected ProgramRegistry registry;
    protected final String operationName;
    protected final List<Instruction> operationInstructions;
    protected final Set<Variable> inputVariables;
    protected final Set<Variable> workVariables;
    protected final Map<Label, Instruction> labelToInstruction;
    protected final List<Label> labelsInOperation;              // keep order
    protected final Set<Label> labelsAddedAfterExtension;     // keep order after expansion
    protected final Set<Label> referencedLabels;
    protected Label entry;


    protected int nextLabelNumber = 1;
    protected int nextWorkVariableNumber = 1;

    // protected ctor for Builder
    protected Operation(Builder<?,?> builder) {
        this.operationName = (builder.operationName == null || builder.operationName.isBlank())
                ? "Unnamed Program" : builder.operationName;

        this.operationInstructions = new ArrayList<>();
        this.labelToInstruction = new LinkedHashMap<>();
        this.inputVariables = new LinkedHashSet<>();
        this.workVariables = new LinkedHashSet<>();
        this.labelsInOperation = new ArrayList<>();
        this.labelsAddedAfterExtension = new LinkedHashSet<>();
        this.referencedLabels = new LinkedHashSet<>();


        // bucket declared variables (even if not used in instructions)
        for (Variable v : builder.variables) {
            if (v == null) continue;
            if (v.getType() == VariableType.INPUT)  inputVariables.add(v);
            else if (v.getType() == VariableType.WORK) workVariables.add(v);
        }

        // keep declared labels in order
        for (Label lbl : builder.labels) {
            if (lbl != null && lbl != FixedLabel.EMPTY && !labelsInOperation.contains(lbl)) {
                labelsInOperation.add(lbl);
            }
        }

        // add instructions via instance addInstruction to keep maps/sets consistent
        for (Instruction ins : builder.operationInstructions) {
            if (ins != null) addInstruction(ins);
        }

        sortVariableSetByNumber(inputVariables);
        sortVariableSetByNumber(workVariables);
        initialize();
    }

    public Optional<Label> getEntry() { return Optional.ofNullable(entry); }

    public Label firstLabeledInstruction() {
        return labelsInOperation.isEmpty() ? FixedLabel.EMPTY : labelsInOperation.getFirst();
    }

    // --------- Builder ----------
    public static abstract class Builder<B extends  Builder<B,T>, T extends Operation> {
        protected String operationName;
        protected final List<Instruction> operationInstructions = new ArrayList<>();
        protected final Set<Variable> variables = new LinkedHashSet<>();
        protected final List<Label> labels = new ArrayList<>();
        protected final Set<Label> referencedLabels = new LinkedHashSet<>();
        protected Label entry;

        protected abstract B self();
        public abstract T build();

        public B withName(String name) {
            this.operationName = name;
            return self();
        }

        public B withInstructions(Collection<? extends Instruction> newInstructions) {
            operationInstructions.clear();
            if (newInstructions != null) operationInstructions.addAll(newInstructions);
            return self();
        }

        public B addInstruction(Instruction instruction) {
            if (instruction != null) operationInstructions.add(instruction);
            return self();
        }

        public B addInstructions(Instruction... instructions) {
            if (instructions != null) operationInstructions.addAll(Arrays.asList(instructions));
            return self();
        }

        public B withVariables(Collection<? extends Variable> newVariables) {
            variables.clear();
            if (newVariables != null) variables.addAll(newVariables);
            return self();
        }

        public B addVariable(Variable v) {
            if (v != null) variables.add(v);
            return self();
        }

        public B addVariables(Variable... vars) {
            if (vars != null) variables.addAll(Arrays.asList(vars));
            return self();
        }

        public B withLabels(Collection<? extends Label> newLabels) {
            labels.clear();
            if (newLabels != null) labels.addAll(newLabels);
            return self();
        }

        public B addLabel(Label l) {
            if (l != null) labels.add(l);
            return self();
        }

        public B addLabels(Label... ls) {
            if (ls != null) labels.addAll(Arrays.asList(ls));
            return self();
        }

        public B withEntry(Label entry) {
            this.entry = entry;
            return self();
        }
    }

    // --------- cloning ----------
    public Operation deepClone() {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
                out.writeObject(this);
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            try (ObjectInputStream in = new ObjectInputStream(bis)) {
                return (Operation) in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed cloning operation", e);
        }
    }

    public void initialize() {
        initNextLabelNumber();
        initNextWorkVariableNumber();
    }

    // --------- Program API ----------
    public String getName() {
        return this.operationName;
    }

    @Override
    public void addInstruction(Instruction instruction) {
        if (instruction == null) return;
        instruction.setProgramOfThisInstruction(this);
        updateVariableAndLabel(instruction);
        operationInstructions.add(instruction);
    }

    @Override
    public void updateVariableAndLabel(Instruction instruction) {
        if (instruction == null) return;
        Label currentLabel = instruction.getLabel();

        bucketLabel(instruction, currentLabel);
        bucketVariable(instruction.getTargetVariable());
        if (instruction instanceof SourceVariableInstruction) {
            bucketVariable(instruction.getSourceVariable());
        }
    }

    private void bucketLabel(Instruction instruction, Label currentLabel) {
        if (currentLabel != FixedLabel.EMPTY) {
            if (!labelToInstruction.containsKey(currentLabel)) {
                labelsInOperation.add(currentLabel);
                labelToInstruction.put(currentLabel, instruction);
            } else {
                throw new IllegalArgumentException(
                        "Duplicate label " + currentLabel.getLabelRepresentation() +
                                " at instructions: " +
                                labelToInstruction.get(currentLabel).getName() +
                                " and " + instruction.getName()
                );
            }
        }

        if (instruction instanceof LabelReferencesInstruction labelReferencesInstruction) {
            Label referenceLabel = labelReferencesInstruction.getReferenceLabel();
            if (referenceLabel != null && referenceLabel != FixedLabel.EMPTY) {
                referencedLabels.add(referenceLabel);
            }
        }
    }

    private void bucketVariable(Variable variable) {
        if (variable == null) return;
        switch (variable.getType()) {
            case INPUT -> inputVariables.add(variable);
            case WORK  -> workVariables.add(variable);
            default -> {}
        }
    }

    @Override
    public List<Instruction> getInstructionsList() {
        return this.operationInstructions;
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
    public void validateProgram() throws EngineLoadException {
        validateLabelReferencesExist();
    }

    private void validateLabelReferencesExist() throws EngineLoadException {
        Set<Label> undefined = new LinkedHashSet<>(referencedLabels);
        undefined.removeAll(labelToInstruction.keySet());
        undefined.remove(FixedLabel.EXIT);

        if (!undefined.isEmpty()) {
            String names = undefined.stream()
                    .filter(lbl -> lbl != FixedLabel.EMPTY && lbl != FixedLabel.EXIT)
                    .map(Label::getLabelRepresentation)
                    .collect(Collectors.joining(", "));
            throw new EngineLoadException("Undefined label reference(s) in the program: " + names);
        }
    }

    private static final Comparator<Label> EXIT_LAST_THEN_NUMBER =
            Comparator.comparing((Label l) -> FixedLabel.EXIT.equals(l))
                    .thenComparingInt(Label::getIndex);

    @Override
    public List<String> getOrderedLabelsExitLastStr() {
        return java.util.stream.Stream
                .concat(labelsInOperation.stream(), referencedLabels.stream())
                .distinct()
                .sorted(EXIT_LAST_THEN_NUMBER)
                .map(Label::getLabelRepresentation)
                .toList();
    }

    @Override
    public List<String> getInputVariablesSortedStr() {
        return inputVariables.stream()
                .sorted(Comparator.comparingInt(Variable::getIndex))
                .map(Variable::getRepresentation)
                .collect(Collectors.toList());
    }

    @Override
    public List<InstructionDTO> getInstructionDTOList() {
        List<InstructionDTO> instructionDTOList = new ArrayList<>();
        for (Instruction currInstruction : operationInstructions) {
            instructionDTOList.add(currInstruction.getInstructionDTO());
        }
        return instructionDTOList;
    }

    @Override
    public List<List<InstructionDTO>> getExpandedProgram() {
        List<List<InstructionDTO>> expandedProgram = new ArrayList<>();

        for (Instruction instruction : operationInstructions) {
            List<InstructionDTO> chain = instruction.getInstructionExtendedList();
            if (chain != null && !chain.isEmpty()) {
                expandedProgram.add(chain);
            }
        }

        return expandedProgram;
    }

    @Override
    public List<Label> getLabelsInProgram() {
        return labelsInOperation;
    }

    @Override
    public Map<Label, Instruction> getLabelToInstruction() {
        return labelToInstruction;
    }

    public void setRegistry(ProgramRegistry registry) { this.registry = registry; }
    public ProgramRegistry getRegistry() { return registry; }

//    @Override
//    public int calculateProgramMaxDegree() {
//        int maxDegree = 0;
//
//        for (Instruction instruction : operationInstructions) {
//            if (instruction instanceof QuoteInstruction quoteInstruction) {
//                //TODO
//            }
//            if (instruction instanceof SyntheticInstruction syntheticInstruction) {
//                maxDegree = Math.max(maxDegree, syntheticInstruction.getMaxDegree());
//            }
//        }
//
//        return maxDegree;
//    }

    @Override
    public void expandProgram(int degree) {
        for (int i = 0 ; i < degree ; i++) {
            int nextInstructionNumber = 1;

            for (ListIterator<Instruction> iterator = getInstructionsList().listIterator(); iterator.hasNext(); ) {
                Instruction currentInstruction = iterator.next();
                Label originalLabel = currentInstruction.getLabel();
                List<Instruction> newInstructionsList = new ArrayList<>();

                // initialize
                currentInstruction.setProgramOfThisInstruction(this);
                if (currentInstruction instanceof SyntheticInstruction syntheticInstruction) {
                    nextInstructionNumber = syntheticInstruction.expandInstruction(nextInstructionNumber);
                    newInstructionsList = currentInstruction.getExtendedInstruction();
                }
                else {
                    Instruction clonedInstruction = currentInstruction.createInstructionWithInstructionNumber(nextInstructionNumber);
                    newInstructionsList.add(clonedInstruction);
                    nextInstructionNumber++;
                }

                iterator.remove();
                getLabelToInstruction().remove(originalLabel);
                getLabelsInProgram().remove(originalLabel);

                for (Instruction extendedInstruction : newInstructionsList) {
                    extendedInstruction.setProgramOfThisInstruction(this);
                    updateVariableAndLabel(extendedInstruction);
                    iterator.add(extendedInstruction);
                }
            }
        }
    }

    private void initNextLabelNumber() {
        nextLabelNumber = labelsInOperation.stream()
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
                    "Attempted to add duplicate labels after extension: " + uniqueLabel.getLabelRepresentation()
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
                .sorted(Comparator.comparingInt(Variable::getIndex))
                .toList();

        variablesSet.clear();
        variablesSet.addAll(sorted);
    }

    @Override
    public void addInputVariable(Variable variable) {
        if (variable != null) inputVariables.add(variable);
    }

    @Override
    public List<Variable> getInputAndWorkVariablesSortedBySerial() {
        sortVariableSetByNumber(inputVariables);
        sortVariableSetByNumber(workVariables);

        List<Variable> inputAndWorkVariablesAndTheirValues = new ArrayList<>(inputVariables);
        inputAndWorkVariablesAndTheirValues.addAll(workVariables);
        return inputAndWorkVariablesAndTheirValues;
    }

    public Variable getResultVariable() {
        return Variable.RESULT;
    }

    @Override
    public  Map<Integer, OperationView> calculateDegreeToProgram() {
        Map<Integer, OperationView>  degreeToProgram = new HashMap<>();
        boolean canExpandMore;
        int degree = 0;

        OperationView workingProgram = this.deepClone();
        workingProgram.setRegistry(this.getRegistry());

        do {
            OperationView snapshotWorkingProgram = workingProgram.deepClone();
            snapshotWorkingProgram.setRegistry(this.getRegistry());

            FunctionDisplayResolver.populateDisplayNames(snapshotWorkingProgram, this.getRegistry());

            degreeToProgram.put(degree, snapshotWorkingProgram);
            int nextInstructionNumber = 1;
            canExpandMore = false;

            for (ListIterator<Instruction> iterator = workingProgram.getInstructionsList().listIterator(); iterator.hasNext(); ) {  // Run on working program
                Instruction instruction = iterator.next();
                instruction.setProgramOfThisInstruction(workingProgram);
                Label originalLabel = instruction.getLabel();
                List<Instruction> newInstructionsList = new ArrayList<>();

                if (instruction instanceof SyntheticInstruction syntheticInstruction) {
                    nextInstructionNumber = syntheticInstruction.expandInstruction(nextInstructionNumber);
                    newInstructionsList = instruction.getExtendedInstruction();
                    canExpandMore = true;
                } else {
                    Instruction cloneInstruction = instruction.createInstructionWithInstructionNumber(nextInstructionNumber);
                    newInstructionsList.add(cloneInstruction);
                    nextInstructionNumber++;
                }

                iterator.remove();                               // Remove the old instruction
                workingProgram.getLabelToInstruction().remove(originalLabel);       // Remove the label from the map because we will add it again in line 239
                workingProgram.getLabelsInProgram().remove(originalLabel);          // Remove the label from the map because we will add it again in line 239

                for (Instruction extendedInstruction : newInstructionsList) {
                    extendedInstruction.setProgramOfThisInstruction(workingProgram);
                    workingProgram.updateVariableAndLabel(extendedInstruction);
                    iterator.add(extendedInstruction);          // Add the extended (inner) instruction to the list
                }
            }

            degree++;
        } while (canExpandMore);

        return degreeToProgram;
    }
}
