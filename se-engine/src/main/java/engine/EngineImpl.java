package engine;

import exceptions.EngineLoadException;
import execution.ProgramExecutorImpl;
import history.ExecutionHistory;
import execution.ProgramExecutor;
import history.ExecutionHistoryImpl;
import program.Program;
import loader.XmlProgramLoader;

import java.nio.file.Path;

public class EngineImpl implements Engine {
    private Path xmlPath;
    private Program program;
    private ProgramExecutor programExecutor;
    private ExecutionHistory executionHistory;


    @Override
    public void loadProgram(Path xmlPath) throws EngineLoadException {
        this.xmlPath = xmlPath;
        Program newProgram;

        XmlProgramLoader loader = new XmlProgramLoader();
        newProgram = loader.load(xmlPath);
        newProgram.validateProgram();
        newProgram.initialize();

        program = newProgram;
        executionHistory = new ExecutionHistoryImpl();
    }

    @Override
    public void runProgram(int degree, Long... inputs) throws EngineLoadException {
        Program deepCopyOfProgram = program.cloneProgram(xmlPath, program.getNextLabelNumber(), program.getNextWorkVariableNumber());

        deepCopyOfProgram.expandProgram(degree);

        programExecutor = new ProgramExecutorImpl(deepCopyOfProgram);

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
    public void displayExpandedProgram(int degree) throws EngineLoadException {
        Program deepCopyOfProgram = program.cloneProgram(xmlPath, program.getNextLabelNumber(), program.getNextWorkVariableNumber());
        deepCopyOfProgram.expandProgram(degree);
        System.out.println(deepCopyOfProgram.getExtendedProgramDisplay());
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
        System.out.println("Cycles: " + programExecutor.getTotalCyclesOfProgram());
    }

    @Override
    public void displayHistory() {
        if (executionHistory.hasHistory()) {
            System.out.println(executionHistory.displayExecutionHistory());
        } else {
            System.out.println("No history has been set in this program. Run the program first.");
        }
    }
}
