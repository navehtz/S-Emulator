package variable;

import java.io.Serializable;
import java.util.Objects;

public class VariableImpl implements Variable , Serializable {
    private final VariableType type;
    private final int index;

    public VariableImpl(VariableType variableType, int index) {
        this.type = variableType;
        this.index = (type == VariableType.RESULT) ? 0 : index;
    }

    @Override
    public VariableType getType() {
        return type;
    }

    @Override
    public String getRepresentation() {
        return type.getVariableRepresentation(index);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof VariableImpl other)) return false;
        return this.index == other.index && this.type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, index);
    }
}
