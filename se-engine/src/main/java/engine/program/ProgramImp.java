package engine.program;

import engine.instruction.Instruction;
import engine.label.FixedLabel;
import engine.label.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProgramImp implements Program {

    private final String programName;
    private final List<Instruction> programInstructions;

    public ProgramImp(String name) {
        this.programName = name;
        this.programInstructions = new ArrayList<>();
    }

    @Override
    public String getName() {
        return this.programName;
    }

    @Override
    public void addInstruction(Instruction instruction) {
        programInstructions.add(instruction);

        if(instruction.getLabel() != FixedLabel.EMPTY) {
            labelToInstruction().put(instruction.getLabel(), instruction);
        }
    }

    @Override
    public List<Instruction> getInstructionsList() {
        return List.of();
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
    public Map<Label, Instruction> labelToInstruction() {
        return Map.of();
    }


}
