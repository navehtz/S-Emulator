package engine.variable;

public enum VariableType {
    INPUT{
        public String getVariableRepresentation(int number) {
            return "x" + number;
        }
    },
    WORK{
        public String getVariableRepresentation(int number) {
            return "z" + number;
        }
    },
    RESULT{
        public String getVariableRepresentation(int number) {
            return "y";
        }
    };

    public abstract String getVariableRepresentation(int number);
}
