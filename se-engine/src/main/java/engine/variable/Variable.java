package engine.variable;

public interface Variable {
    VariableType getType();
    String getRepresentation();

    // TODO: ask aviad how to convert it to static initializer
    Variable RESULT = new VariableImpl(VariableType.RESULT, 0);

}
