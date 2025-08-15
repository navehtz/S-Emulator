package engine.program;

import engine.instruction.Instruction;
import engine.label.Label;

import java.util.List;
import java.util.Map;

public interface Program {
    String getName();
    void addInstruction(Instruction instruction);
    List<Instruction> getInstructionsList();

    int calculateMaxDegree();
    int calculateCycles();

    Map<Label, Instruction> labelToInstruction();
}
