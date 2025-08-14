package engine.domain;

import java.util.Objects;

public class Instruction {
    private final int number;
    private final Kind kind;
    private final String label;
    private final String command;
    private final int cycles;

    private static final int LABEL_SIZE = 3;

    public Instruction(int number, Kind kind, String label, String command, int cycles) {
        if (number < 1) throw new IllegalArgumentException("Number must be >= 1");
        if (cycles < 0) throw new IllegalArgumentException("Cycle must be >= 0");

        this.number = number;
        this.kind = Objects.requireNonNull(kind, "kind is null");
        this.label = label != null && !label.isBlank() ? label.trim() : "";
        this.command = Objects.requireNonNull(command, "command cannot be null");
        this.cycles = cycles;
    }

    public int getNumber() {
        return number;
    }

    public Kind getKind() {
        return kind;
    }

    public String getLabel() {
        return label;
    }

    public String getCommand() {
        return command;
    }

    public int getCycle() {
        return cycles;
    }

    @Override
    public String toString() {
        String type = (kind == Kind.BASIC) ? "B" : "S";

        return String.format("#%-2d (%s) [%-5s] %s (%d)",
                number,
                type,
                label,
                command,
                cycles);
    }
}
