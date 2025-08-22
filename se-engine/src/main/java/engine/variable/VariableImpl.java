package engine.variable;

public class VariableImpl implements Variable {
    private final VariableType type;
    private final int number;

    public VariableImpl(VariableType variableType, int number) {
        this.type = variableType;
        this.number = (type == VariableType.RESULT) ? 0 : number;       // y will always get 0
    }

    @Override
    public VariableType getType() {
        return type;
    }

    @Override
    public String getRepresentation() {
        return type.getVariableRepresentation(number);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VariableImpl)) return false;

        VariableImpl other = (VariableImpl) obj;
        return this.number == other.number && this.type == other.type;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(type, number);
    }
}
