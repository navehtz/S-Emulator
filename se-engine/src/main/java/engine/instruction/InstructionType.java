package engine.instruction;

public enum InstructionType {

    BASIC("B"),
    SYNTHETIC("S")
    ;

    private String instructionType;

    InstructionType(String instructionType) {
        this.instructionType = instructionType;
    }

/*    BASIC {
        @Override
        public String getInstructionType() {
            return "B";
        }
    },
    SYNTHETIC {
        @Override
        public String getInstructionType() {
            return "S";
        }
    };

    public abstract String getInstructionType();*/
}