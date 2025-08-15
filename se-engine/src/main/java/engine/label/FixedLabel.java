package engine.label;

public enum FixedLabel implements Label {

    EXIT {
        @Override
        public String getLabelRepresentation() {
            return "EXIT";
        }
    },
    EMPTY {
        @Override
        public String getLabelRepresentation() {
            return "EMPTY";
        }
    };

    @Override
    public abstract String getLabelRepresentation();
}
