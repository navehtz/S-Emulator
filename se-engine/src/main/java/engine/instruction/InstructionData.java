package engine.instruction;

public enum InstructionData {

    INCREASE("INCREASE", 1),
    DECREASE("DECREASE", 1),
    JUMP_NOT_ZERO("JNZ", 2),
    NO_OP("NO_OP", 0)
    ;

    private final String name;
    private final int cycles;

    InstructionData(String name, int cycles) {
        this.name = name;
        this.cycles = cycles;
    }

    public String getName() {
        return name;
    }

    public int getCycles() {
        return cycles;
    }
}
