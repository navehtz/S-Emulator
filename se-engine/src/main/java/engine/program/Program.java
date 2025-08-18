package engine.program;

import engine.instruction.Instruction;
import engine.label.Label;
import engine.variable.Variable;

import java.util.List;
import java.util.Set;

public interface Program {
    String getName();
    void addInstruction(Instruction instruction);
    List<Instruction> getInstructionsList();

    int calculateMaxDegree();
    int calculateCycles();

    Instruction getInstructionByLabel(Label label);
    Set<Variable> getInputVariables();
    String getProgramDisplay();

    String programRepresentation();
}
