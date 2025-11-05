package instruction;

import architecture.ArchitectureType;

import static architecture.ArchitectureType.*;

public enum InstructionData {

    ORIGIN("ORIGIN", 0, A_0),

    INCREASE("INCREASE", 1, A_1),
    DECREASE("DECREASE", 1, A_1),
    JUMP_NOT_ZERO("JNZ", 2, A_2),
    NO_OP("NO_OP", 0, A_1),
    ZERO_VARIABLE("ZERO_VARIABLE", 1, A_2),
    GOTO_LABEL("GOTO_LABEL", 1, A_2),
    ASSIGNMENT("ASSIGNMENT", 4, A_3),
    CONSTANT_ASSIGNMENT("CONSTANT_ASSIGNMENT", 2, A_2),
    JUMP_ZERO("JUMP_ZERO", 2, A_3),
    JUMP_EQUAL_CONSTANT("JUMP_EQUAL_CONSTANT", 2, A_3),
    JUMP_EQUAL_VARIABLE("JUMP_EQUAL_VARIABLE", 2, A_3),
    QUOTE("QUOTE", 5, A_4),
    JUMP_EQUAL_FUNCTION("JUMP_EQUAL_FUNCTION", 6, A_4)
    ;

    private final String name;
    private final int cycles;
    private final ArchitectureType architectureType;

    InstructionData(String name, int cycles, ArchitectureType architectureType) {
        this.name = name;
        this.cycles = cycles;
        this.architectureType = architectureType;
    }

    public String getName() {
        return name;
    }

    public int getCycles() {
        return cycles;
    }

    public ArchitectureType getArchitectureType() {
        return architectureType;
    }
}
