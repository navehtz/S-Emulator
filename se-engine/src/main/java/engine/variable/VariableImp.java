package engine.variable;

public class VariableImp implements Variable {
    private final VariableType type;
    private final int number;

    public VariableImp(VariableType variableType, int number) {
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
