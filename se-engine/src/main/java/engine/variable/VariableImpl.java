package engine.variable;

public class VariableImpl implements Variable {
    private final VariableType type;
    private final int number;

    public VariableImpl(VariableType variableType, int number) {
        this.type = variableType;
        this.number = number;
    }

    @Override
    public VariableType getType() {
        return type;
    }

    @Override
    public String getRepresentation() {
        return type.getVariableRepresentation(number);
    }
}
