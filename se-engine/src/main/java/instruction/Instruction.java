package instruction;

import dto.InstructionDTO;
import execution.ExecutionContext;
import label.Label;
import operation.Operation;
import program.Program;
import variable.Variable;

import java.io.Serializable;
import java.util.List;

public interface Instruction extends Serializable {

    String getName();
    String getInstructionType();
    Label getLabel();
    Label getReferenceLabel();
    Variable getTargetVariable();
    Variable getSourceVariable();
    int getInstructionNumber();
    String getCommand();
    List<Instruction> getExtendedInstruction();
    int getCycleOfInstruction();
    Instruction getOriginalInstruction();
    InstructionDTO getInstructionDTO();
    List<InstructionDTO> getInstructionExtendedList();

    void setProgramOfThisInstruction(Operation operationOfThisInstruction);
    Label execute(ExecutionContext context);
    Instruction createInstructionWithInstructionNumber(int instructionNumber);
}
