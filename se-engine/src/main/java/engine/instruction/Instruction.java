package engine.instruction;

import engine.execution.ExecutionContext;
import engine.label.Label;
import engine.variable.Variable;

public interface Instruction {
    String getName();
    int getCycles();
    Label execute(ExecutionContext context);
    Label getLabel();
    Variable getTargetVariable();
    Variable getSourceVariable();
    String getCommand();
    String instructionRepresentation(int instructionNumber);
}
