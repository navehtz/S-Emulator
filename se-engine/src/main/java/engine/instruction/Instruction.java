package engine.instruction;

import engine.execution.ExecutionContext;
import engine.label.Label;
import engine.program.Program;
import engine.variable.Variable;

import java.util.List;

public interface Instruction {
    String getName();
    Label execute(ExecutionContext context);

    Instruction createNewInstructionWithNewLabel(Label newLabel);
    Label getLabel();
    Variable getTargetVariable();
    Variable getSourceVariable();
    String getCommand();
    List<Instruction> getExtendedInstruction();
    int getCycleOfInstruction();

    void setProgramOfThisInstruction(Program programOfThisInstruction);

    String instructionRepresentation(int numberOfInstructionsInProgram, int instructionNumber);
    int calculateInstructionMaxDegree(Program program);

}
