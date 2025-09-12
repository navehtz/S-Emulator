package label;

public enum FixedLabel implements Label{

    EXIT {
        @Override
        public String getLabelRepresentation() {
            return "EXIT";
        }
        public int getIndex() { return 0; }
    },
    EMPTY {
        @Override
        public String getLabelRepresentation() {
            return "";
        }
        public int getIndex() { return 0; }
    };

    @Override
    public abstract String getLabelRepresentation();

    @Override
    public abstract int getIndex();
}
