package engine.program;

import engine.instruction.Instruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.variable.Variable;
import engine.variable.VariableType;

import java.util.*;

public class ProgramImpl implements Program {

    private final String programName;
    private final List<Instruction> programInstructions;
    private final Map<Label, Instruction> labelToInstruction;
    private final Set<Variable> inputVariables = new LinkedHashSet<>();

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

        if(instruction.getLabel() != FixedLabel.EMPTY) {                    // Add label and its instruction to map
            labelToInstruction.put(instruction.getLabel(), instruction);
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

}
