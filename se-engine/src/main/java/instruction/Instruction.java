package instruction;

import dto.InstructionDTO;
import execution.ExecutionContext;
import label.Label;
import operation.OperationView;
import variable.Variable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface Instruction extends Serializable {

    String getName();
    String getInstructionType();
    Label getLabel();
    Label getReferenceLabel();
    Variable getTargetVariable();
    int getInstructionNumber();
    Variable getSourceVariable();
    String getCommand();
    List<Instruction> getExtendedInstruction();
    int getCycleOfInstruction();
    Instruction getOriginalInstruction();
    InstructionDTO getInstructionDTO();
    List<InstructionDTO> getInstructionExtendedList();

    OperationView getProgramOfThisInstruction();

    void setProgramOfThisInstruction(OperationView operationOfThisInstruction);
    Label execute(ExecutionContext context);
    Instruction createInstructionWithInstructionNumber(int instructionNumber);

    default Instruction remapAndClone(
            int newInstructionNumber,
            Map<Variable, Variable> varMap,
            Map<label.Label, label.Label> labelMap,
            Instruction newOrigin,
            OperationView newOwner
    ) {
        // Fallback: just renumber (if an impl forgets to override)
        Instruction c = createInstructionWithInstructionNumber(newInstructionNumber);
        c.setProgramOfThisInstruction(newOwner);
        return c;
    }
}
