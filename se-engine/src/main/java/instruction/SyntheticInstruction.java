package instruction;

import java.util.List;

public interface SyntheticInstruction {
    List<Instruction> getInnerInstructions();
    int getMaxDegree();

    int expandInstruction(int startNumber);
}
