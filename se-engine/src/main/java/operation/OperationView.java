package operation;

import dto.execution.InstructionDTO;
import engine.ProgramRegistry;
import exceptions.EngineLoadException;
import instruction.Instruction;
import label.Label;
import variable.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OperationView {
    String getName();

    List<Instruction> getInstructionsList();

    Instruction getInstructionByLabel(Label label);

    Map<Label, Instruction> getLabelToInstruction();

    List<Label> getLabelsInProgram();

    Set<Variable> getInputVariables();

    Set<Variable> getWorkVariables();

    // Keep the rest as needed:
    List<String> getOrderedLabelsExitLastStr();

    List<String> getInputVariablesSortedStr();

    List<InstructionDTO> getInstructionDTOList();

    List<List<InstructionDTO>> getExpandedProgram();

    ProgramRegistry getRegistry();

    void setRegistry(ProgramRegistry registry);

    Map<Integer, OperationView> calculateDegreeToProgram();

    void validateProgram() throws EngineLoadException;

    Label generateUniqueLabel();

    Variable generateUniqueVariable();

    void addInstruction(Instruction instruction);

    void addInputVariable(Variable variable);

    void expandProgram(int degree);

    void sortVariableSetByNumber(Set<Variable> variablesSet);

    List<Variable> getInputAndWorkVariablesSortedBySerial();

    void initialize();

    public Operation deepClone();

    void updateVariableAndLabel(Instruction instruction);

    String getUserUploaded();

}

