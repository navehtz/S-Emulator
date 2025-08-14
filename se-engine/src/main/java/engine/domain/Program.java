package engine.domain;

import java.util.List;
import java.util.Objects;

public final class Program {

    private final String name;
    private final List<Instruction> instructions;
    private final List<String> inputsOrdered;
    private final List<String> labelsOrdered;

    public Program(String name,
                   List<Instruction> instructions,
                   List<String> inputsOrdered,
                   List<String> labelsOrdered) {
        this.name = name;
        this.instructions = List.copyOf(Objects.requireNonNull(instructions, "instructions cannot be null"));
        this.inputsOrdered = List.copyOf(Objects.requireNonNull(inputsOrdered, "inputsOrdered"));
        this.labelsOrdered = List.copyOf(Objects.requireNonNull(labelsOrdered, "labelsOrdered"));
    }

    public List<Instruction> instructions() {
        return instructions;
    }

    public String name() {
        return name;
    }

    public List<String> inputsOrdered() {
        return inputsOrdered;
    }

    public List<String> labelsOrdered() {
        return labelsOrdered;
    }
}
