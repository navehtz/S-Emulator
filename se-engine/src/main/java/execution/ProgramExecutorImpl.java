package execution;

import instruction.Instruction;
import label.FixedLabel;
import label.Label;
import program.Program;
import variable.Variable;

import java.util.*;

public class ProgramExecutorImpl implements ProgramExecutor{

    private final Program program;
    private final ExecutionContext context;
    private List<Long> inputsValues;
    private int runDegree = 0;
    private int totalCycles = 0;


    public ProgramExecutorImpl(Program program) {
        this.program = program;
        this.context = new ExecutionContextImpl();
        this.inputsValues = new ArrayList<Long>();
    }

    @Override
    public long run(int runDegree, Long... inputs) {
        Instruction currentInstruction = program.getInstructionsList().getFirst();
        Instruction nextInstruction = null;
        Label nextLabel;

        inputsValues = List.of(inputs);
        context.initializeVariables(program, inputs);
        this.runDegree = runDegree;

        do {
                nextLabel = currentInstruction.execute(context);
                totalCycles += currentInstruction.getCycleOfInstruction();

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
    public long getVariableValue(Variable variable) {
        return context.getVariableValue(variable);
    }


    @Override
    public int getRunDegree() {
        return runDegree;
    }



    @Override
    public List<Long> getInputsValuesOfUser() {
        return inputsValues;
    }

    @Override
    public int getTotalCyclesOfProgram() {
        return this.totalCycles;
    }

    @Override
    public long getResultValue() {
        return context.getVariableValue(Variable.RESULT);
    }

/*    @Override
    public String getProgramAfterRun() {
        return program.getProgramDisplay();
    }*/

    @Override
    public Set<Variable> getInputVariablesSet() {
        return program.getInputVariables();
    }

    private Map<Variable, Long> getInputAndWorkVariablesAndTheirValuesMap() {

        List<Variable> variableList = program.getInputAndWorkVariablesSortedBySerial();
        Map<Variable, Long> variableToValue = new LinkedHashMap<Variable, Long>();

        for (Variable variable : variableList) {
            variableToValue.put(variable, context.getVariableValue(variable));
        }

        return variableToValue;
    }

    @Override
    public String getVariablesWithValuesSortedString() {
        StringBuilder variablesDisplay = new StringBuilder();
        Map<Variable, Long> variableToValue = getInputAndWorkVariablesAndTheirValuesMap();

        variablesDisplay.append("y").append(" = ").append(getResultValue()).append(System.lineSeparator());

        for (Map.Entry<Variable, Long> entry : variableToValue.entrySet()) {
            Variable key = entry.getKey();
            String v = key.getRepresentation();
            Long value = entry.getValue();

            variablesDisplay.append(v).append(" = ").append(value).append(System.lineSeparator());
        }

        return variablesDisplay.toString();
    }

    @Override
    public Map<String, Long> getVariablesToValuesSorted() {
        Map<String, Long> VariablesToValuesSorted = new LinkedHashMap<>();

        for (Variable v : program.getInputAndWorkVariablesSortedBySerial()) {
            VariablesToValuesSorted.put(v.getRepresentation(), context.getVariableValue(v));
        }

        return VariablesToValuesSorted;
    }

    /*    @Override
    public void extendProgram(int degree) {
        if (degree > 0) {
            runDegree = degree;
            program.extendProgram(degree);
        }
    }*/

    /*    @Override
    public String getExtendedProgramDisplay() {
        List<String> extendedDisplay = program.getExtendedProgramDisplay();
        StringBuilder extendedProgramDisplay = new StringBuilder();

        for(String line : extendedDisplay) {
            extendedProgramDisplay.append(line).append(System.lineSeparator());
        }

        return extendedProgramDisplay.toString();
    }*/

/*    @Override
    public int calculateProgramMaxDegree() {
        return program.calculateProgramMaxDegree();
    }*/
}