package engine.program;

import engine.instruction.Instruction;

import java.util.ArrayList;
import java.util.List;

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
    }

    @Override
    public List<Instruction> getInstructions() {
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
}
