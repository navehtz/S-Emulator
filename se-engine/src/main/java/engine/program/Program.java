package engine.program;

import engine.instruction.Instruction;

import java.util.List;

public interface Program {
    String getName();
    void addInstruction(Instruction instruction);
    List<Instruction> getInstructions();

    int calculateMaxDegree();
    int calculateCycles();
}
