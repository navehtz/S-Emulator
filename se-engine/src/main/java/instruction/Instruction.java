package instruction;

import execution.ExecutionContext;
import label.Label;
import program.Program;
import variable.Variable;

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
    List<String> getInstructionExtendedDisplay(int numberOfInstructionsInProgram);
    String getInstructionRepresentation(int numberOfInstructionsInProgram);

    void setProgramOfThisInstruction(Program programOfThisInstruction);
    Label execute(ExecutionContext context);
    Instruction createInstructionWithInstructionNumber(int instructionNumber);
    //int calculateInstructionMaxDegree(Program program);

}
