package label;

public class LabelImpl implements Label {

    private final String labelStr;
    private final int number;

    public LabelImpl(int number) {

        this.labelStr = "L" + number;
        this.number = number;
    }

    @Override
    public String getLabelRepresentation() {
        return labelStr;
    }

    @Override
    public int getNumber() {
        return number;
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
