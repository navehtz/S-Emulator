package engine;

import exceptions.EngineLoadException;
import execution.ProgramExecutorImpl;
import history.ExecutionHistory;
import execution.ProgramExecutor;
import history.ExecutionHistoryImpl;
import program.Program;
import variable.Variable;
import loader.XmlProgramLoader;

import java.nio.file.Path;

public class EngineImpl implements Engine {

    private Program program;
    private ProgramExecutor programExecutor;
    private ExecutionHistory executionHistory;


    @Override
    public void loadProgram(Path xmlPath) throws EngineLoadException {
        XmlProgramLoader loader = new XmlProgramLoader();
        executionHistory = new ExecutionHistoryImpl();
        program = loader.load(xmlPath);
        program.validateProgram();
        program.initialize();
    }

    @Override
    public void runProgram(int degree, Long... inputs) {
        Program expandedProgram = program.expandProgram(degree);

        programExecutor = new ProgramExecutorImpl(expandedProgram);

        programExecutor.run(degree, inputs);
        executionHistory.addProgramToHistory(programExecutor);
    }

    @Override
    public int getNumberOfInputVariables() {
        return program.getInputVariables().size();
    }

    @Override
    public void displayProgram() {
        System.out.println(program.getProgramDisplay());
    }

    @Override
    public int getMaxDegree() {
        return program.calculateProgramMaxDegree();
    }

    @Override
    public void displayExpandedProgram(int degree) {

        if (degree > 0) {
            Program expandedProgram = program.expandProgram(degree);
            System.out.println(expandedProgram.getExtendedProgramDisplay());
        } else {
            System.out.println("You chose not to expand the program. Displaying original program: ");
            System.out.println();
            System.out.println(program.getProgramDisplay());
        }


    }

    @Override
    public void displayUsedInputVariables() {
        if (program.getInputVariables().isEmpty()) {
            System.out.println("No input variables have been set in this program.");
            return;
        }

        String inputDisplay = program.getInputVariableSorted().stream()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        System.out.println("Input used in program: " + inputDisplay);
    }

    @Override
    public void displayProgramAfterRun() {
        System.out.println(programExecutor.getProgramAfterRun());
        System.out.println("Result: " + programExecutor.getResultValue());
        System.out.println();
        System.out.println(programExecutor.getVariablesWithValuesSortedString());
        System.out.println("Total Cycles: " + programExecutor.getTotalCyclesOfProgram());
    }

    @Override
    public void displayHistory() {
        System.out.println(executionHistory.displayExecutionHistory());
    }
}
