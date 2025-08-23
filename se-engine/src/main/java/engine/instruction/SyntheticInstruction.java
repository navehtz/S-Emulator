package engine.instruction;

import java.util.List;

public interface SyntheticInstruction {
    List<Instruction> getInnerInstructions();
    int getMaxDegree();

    int setInnerInstructionsAndReturnTheNextOne(int startNumber);
}

