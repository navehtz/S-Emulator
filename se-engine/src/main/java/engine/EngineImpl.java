package engine;

import dto.InstructionsDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;
import execution.ProgramExecutorImpl;
import history.ExecutionHistory;
import execution.ProgramExecutor;
import history.ExecutionHistoryImpl;
import program.Program;
import loader.XmlProgramLoader;
import variable.Variable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


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
    public ProgramDTO getProgramToDisplay() {
        InstructionsDTO instructionsDTO = new InstructionsDTO(program.gerInstructionsAsStringList());

        return new ProgramDTO(
                program.getName(),
                program.getOrderedLabelsExitLastStr(),
                program.getInputVariablesSortedStr(),
                instructionsDTO,
                program.getExpandedProgram()
        );
    }

    @Override
    public ProgramExecutorDTO getProgramToDisplayAfterRun() {
        InstructionsDTO instructionsDTO = new InstructionsDTO(program.gerInstructionsAsStringList());

        ProgramDTO programDTO = new ProgramDTO(
                program.getName(),
                program.getOrderedLabelsExitLastStr(),
                program.getInputVariablesSortedStr(),
                instructionsDTO,
                program.getExpandedProgram()
        );

        return new ProgramExecutorDTO(programDTO,
                programExecutor.getVariablesToValuesSorted(),
                programExecutor.getVariableValue(Variable.RESULT),
                programExecutor.getTotalCyclesOfProgram(),
                programExecutor.getRunDegree(),
                programExecutor.getInputsValuesOfUser()
        );
    }

    @Override
    public List<ProgramExecutorDTO> getHistoryToDisplay() {
        List<ProgramExecutorDTO> res = new ArrayList<>();

        for(ProgramExecutor programExecutor : executionHistory.getProgramsExecutions()) {

            InstructionsDTO instructionsDTO = new InstructionsDTO(program.gerInstructionsAsStringList());

            ProgramDTO programDTO = new ProgramDTO(
                    program.getName(),
                    program.getOrderedLabelsExitLastStr(),
                    program.getInputVariablesSortedStr(),
                    instructionsDTO,
                    program.getExpandedProgram()
            );

            ProgramExecutorDTO programExecutorDTO = new ProgramExecutorDTO(programDTO,
                    programExecutor.getVariablesToValuesSorted(),
                    programExecutor.getVariableValue(Variable.RESULT),
                    programExecutor.getTotalCyclesOfProgram(),
                    programExecutor.getRunDegree(),
                    programExecutor.getInputsValuesOfUser()
            );

            res.add(programExecutorDTO);
        }

        return res;
    }

    @Override
    public int getMaxDegree() {
        return program.calculateProgramMaxDegree();
    }

    @Override
    public ProgramDTO getExpandedProgramToDisplay(int degree) throws EngineLoadException {
        Program deepCopyOfProgram = program.cloneProgram(xmlPath, program.getNextLabelNumber(), program.getNextWorkVariableNumber());
        deepCopyOfProgram.expandProgram(degree);

        InstructionsDTO instructionsDTO = new InstructionsDTO(deepCopyOfProgram.gerInstructionsAsStringList());

        return new ProgramDTO(
                deepCopyOfProgram.getName(),
                deepCopyOfProgram.getOrderedLabelsExitLastStr(),
                deepCopyOfProgram.getInputVariablesSortedStr(),
                instructionsDTO,
                deepCopyOfProgram.getExpandedProgram()
        );
    }
}
