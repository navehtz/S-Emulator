package engine.instruction;

import java.util.List;

public interface SyntheticInstruction {
    List<Instruction> getInnerInstructions();
    void setInnerInstructions();
}
