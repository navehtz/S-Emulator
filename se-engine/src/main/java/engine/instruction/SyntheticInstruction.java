package engine.instruction;

import engine.label.Label;

import java.util.List;

public interface SyntheticInstruction {
    List<Instruction> getInnerInstructions();
    void setInnerInstructions();
}
