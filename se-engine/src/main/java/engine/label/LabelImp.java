package engine.label;

public class LabelImp implements Label {

    private final String label;

    public LabelImp(int number) {
        this.label = "L" + number;
    }

    @Override
    public String getLabelRepresentation() {
        return label;
    }
}
