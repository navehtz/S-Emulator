package program;

import exceptions.EngineLoadException;
import instruction.Instruction;
import label.Label;
import variable.Variable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Program {

    void setNextLabelNumber(int nextLabelNumber);
    void setNextWorkVariableNumber(int nextLabelNumber);

    int getNextLabelNumber();
    int getNextWorkVariableNumber();
    List<Instruction> getInstructionsList();
    Instruction getInstructionByLabel(Label label);
    Set<Variable> getInputVariables();
    List<String> getInputVariableSorted();
    Set<Variable> getWorkVariables();
    String getProgramDisplay();
    List<Variable> getInputAndWorkVariablesSortedBySerial();
    Set<Label> getLabelsInProgram();
    Map<Label, Instruction> getLabelToInstruction();
    Set<Label> getReferencedLabels();
    String getExtendedProgramDisplay();
    Set<Label> getLabelsAddedAfterExtension();

    String getName();
    void initialize();
    void initializeByOtherProgram(Program originalProgram);
    void addInstruction(Instruction instruction);
    void addInputVariable(Variable variable);
    void validateProgram() throws EngineLoadException;
    int calculateProgramMaxDegree();
    Program expandProgram(int degree);
    public Program expandByOneDegree(Program originalProgram);
    void sortVariableSetByNumber(Set<Variable> variables);
    Label generateUniqueLabelAndUpdateNextLabelNumber();
    Variable generateUniqueVariableAndUpdateNextVariableNumber();

}
