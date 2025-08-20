package engine.label;

public class LabelImpl implements Label {

    private final String labelStr;

    public LabelImpl(int number) {
        this.labelStr = "L" + number;
    }

    @Override
    public String getLabelRepresentation() {
        return labelStr;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LabelImpl)) return false;

        LabelImpl other = (LabelImpl) obj;
        return this.labelStr.equals(other.labelStr);
    }

    @Override
    public int hashCode() {
        return labelStr.hashCode();
    }
}
