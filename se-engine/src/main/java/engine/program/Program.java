package engine.program;

import engine.exceptions.EngineLoadException;
import engine.instruction.Instruction;
import engine.label.Label;
import engine.variable.Variable;

import java.util.List;
import java.util.Set;

public interface Program {

    String getName();
    List<Instruction> getInstructionsList();
    Instruction getInstructionByLabel(Label label);
    Set<Variable> getInputVariables();
    Set<Variable> getWorkVariables();
    String getProgramDisplay();
    int getTotalCyclesOfProgram();
    List<String> getExtendedProgramDisplay();
    List<Variable> getInputAndWorkVariablesSortedBySerial();

    void addInstruction(Instruction instruction);
    void validateProgram() throws EngineLoadException;
    int calculateProgramMaxDegree();
    void extendProgram(int degree);
    void initialize();
    Label generateUniqueLabel();
    Variable generateUniqueVariable();
    void sortVariableSetByNumber(Set<Variable> variables);
    void addInputVariable(Variable variable);
}
