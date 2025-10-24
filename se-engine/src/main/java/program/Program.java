package program;

import engine.ProgramRegistry;
import label.Label;
import operation.OperationView;

import java.io.Serializable;

public interface Program extends OperationView, Serializable {

    Label entry();

    public void setRegistry(ProgramRegistry registry);

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
