package engine.domain;

import java.util.Objects;

public class InstructionArgument {
    private String name;
    private String value;

    public InstructionArgument(String name, String value) {
        this.name = Objects.requireNonNull(name, "name").trim();
        this.value = Objects.requireNonNull(value, "value").trim();

        if(this.name.isEmpty()) throw new IllegalArgumentException("name cannot be empty");
        if(this.value.isEmpty()) throw new IllegalArgumentException("value cannot be empty");
    }

    public String getName() { return name; }
    public String getValue() { return value; }
}
