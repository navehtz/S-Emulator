package engine.execution;

import dto.ProgramApi;
import engine.instruction.Instruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.program.Program;
import engine.variable.Variable;

import java.util.*;

public class ProgramExecutorImpl implements ProgramExecutor{

    private final Program program;
    ExecutionContext context;
    List<Long> inputsValues;
    int runDegree = 0;
    long result = 0;

    public ProgramExecutorImpl(Program program) {
        this.program = program;
        this.context = new ExecutionContextImpl();
        this.inputsValues = new ArrayList<Long>();
    }

    @Override
    public long run(Long... inputs) {
        Instruction currentInstruction = program.getInstructionsList().getFirst();
        Instruction nextInstruction = null;
        Label nextLabel;

        inputsValues = List.of(inputs);
        context.initializeVariables(program, inputs);

        do {
                nextLabel = currentInstruction.execute(context);

                if (nextLabel == FixedLabel.EMPTY) {
                    int indexOfNextInstruction = program.getInstructionsList().indexOf(currentInstruction) + 1;

                    // If there is more instructions, else Exit
                    if (indexOfNextInstruction < program.getInstructionsList().size()) {
                        nextInstruction = program.getInstructionsList().get(indexOfNextInstruction);
                    } else {
                        nextLabel = FixedLabel.EXIT;
                    }
                } else if (nextLabel != FixedLabel.EXIT) {
                        nextInstruction = program.getInstructionByLabel(nextLabel);
                }

                currentInstruction = nextInstruction;

        } while(nextLabel != FixedLabel.EXIT);

        return context.getVariableValue(Variable.RESULT);
    }

    @Override
    public Map<Variable, Long> variableState() {
        return context.getVariableState();
    }

    @Override
    public long getVariableValue(Variable variable) {
        return context.getVariableValue(variable);
    }

    @Override
    public String getProgramDisplay() {
        return program.getProgramDisplay();
    }

    @Override
    public ProgramApi getProgramApi() {
        return new ProgramApi(getProgramDisplay());
    }

    @Override
    public int getRunDegree() {
        return runDegree;
    }

    @Override
    public String getExtendedProgramDisplay() {
        List<String> extendedDisplay = program.getExtendedProgramDisplay();
        StringBuilder extendedProgramDisplay = new StringBuilder();

        for(String line : extendedDisplay) {
            extendedProgramDisplay.append(line).append(System.lineSeparator());
        }

        return extendedProgramDisplay.toString();
    }

    @Override
    public List<Long> getInputsValues() {
        return inputsValues;
    }

    @Override
    public int getTotalCyclesOfProgram() {
        return program.getTotalCyclesOfProgram();
    }

    @Override
    public void extendProgram(int degree) {
        if (degree > 0) {
            runDegree = degree;
            program.extendProgram(degree);
        }
    }

    @Override
    public Set<Variable> getInputVariablesSet() {
        return program.getInputVariables();
    }

    @Override
    public Map<Variable, Long> getInputAndWorkVariablesAndTheirValuesMap() {

        List<Variable> variableList = program.getInputAndWorkVariablesSortedBySerial();
        Map<Variable, Long> variableToValue = new LinkedHashMap<Variable, Long>();

        for (Variable variable : variableList) {
            variableToValue.put(variable, context.getVariableValue(variable));
        }

        return variableToValue;
    }

    @Override
    public String getInputAndWorkVariablesWithValuesDisplay() {
        StringBuilder variablesDisplay = new StringBuilder();
        Map<Variable, Long> variableToValue = getInputAndWorkVariablesAndTheirValuesMap();

        for (Map.Entry<Variable, Long> entry : variableToValue.entrySet()) {
            Variable key = entry.getKey();
            String v = key.getRepresentation();
            Long value = entry.getValue();

            variablesDisplay.append(v).append(" = ").append(value).append(System.lineSeparator());
        }

        return variablesDisplay.toString();
    }

    @Override
    public int calculateProgramMaxDegree() {
        return program.calculateProgramMaxDegree();
    }
}