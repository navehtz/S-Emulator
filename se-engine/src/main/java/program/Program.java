package program;

import dto.InstructionDTO;
import exceptions.EngineLoadException;
import instruction.Instruction;
import label.Label;
import variable.Variable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Program extends Serializable {

    Label entry();

//    String getName();
//    List<Instruction> getInstructionsList();
//    Instruction getInstructionByLabel(Label label);
//    Set<Variable> getInputVariables();
//    Set<Variable> getWorkVariables();
//    List<Variable> getInputAndWorkVariablesSortedBySerial();
//    List<Label> getLabelsInProgram();
//    Map<Label, Instruction> getLabelToInstruction();
//    List<String> getOrderedLabelsExitLastStr();
//    List<String> getInputVariablesSortedStr();
//    List<List<InstructionDTO>> getExpandedProgram();
//    List<InstructionDTO> getInstructionDTOList();
//
//    Program deepClone();
//    void addInstruction(Instruction instruction);
//    void validateProgram() throws EngineLoadException;
//    int calculateProgramMaxDegree();
//    void expandProgram(int degree);
//    void initialize();
//    Label generateUniqueLabel();
//    Variable generateUniqueVariable();
//    void sortVariableSetByNumber(Set<Variable> variables);
//    void addInputVariable(Variable variable);
}
