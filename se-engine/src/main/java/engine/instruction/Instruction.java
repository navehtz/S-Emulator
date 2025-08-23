package engine.instruction;

import engine.execution.ExecutionContext;
import engine.label.Label;
import engine.program.Program;
import engine.variable.Variable;

import java.util.List;

public interface Instruction {

    String getName();
    Label getLabel();
    Variable getTargetVariable();
    Variable getSourceVariable();
    int getInstructionNumber();
    String getCommand();
    List<Instruction> getExtendedInstruction();
    int getCycleOfInstruction();
    Instruction getOriginalInstruction();
    String getInstructionExtendedDisplay(int numberOfInstructionsInProgram);
    String getInstructionRepresentation(int numberOfInstructionsInProgram);

    void setProgramOfThisInstruction(Program programOfThisInstruction);
    Label execute(ExecutionContext context);
    Instruction createInstructionWithInstructionNumber(int instructionNumber);
    //int calculateInstructionMaxDegree(Program program);

}
