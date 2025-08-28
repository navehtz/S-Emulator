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

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class EngineImpl implements Engine, Serializable {
    private transient Path xmlPath;
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
        return buildProgramDTO(program);
    }

    @Override
    public ProgramExecutorDTO getProgramToDisplayAfterRun() {
        ProgramDTO programDTO = buildProgramDTO(programExecutor.getProgram());

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

            ProgramDTO programDTO = buildProgramDTO(program);

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

    private ProgramDTO buildProgramDTO(Program p) {
        InstructionsDTO instructionsDTO = new InstructionsDTO(p.gerInstructionsAsStringList());
        return new ProgramDTO(
                p.getName(),
                p.getOrderedLabelsExitLastStr(),
                p.getInputVariablesSortedStr(),
                instructionsDTO,
                p.getExpandedProgram()
        );
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

    @Override
    public void saveState(Path path) throws EngineLoadException {
        try {
            EngineIO.save(this, path);   // שימוש במתודה הקיימת
        } catch (IOException e) {
            throw new EngineLoadException("Failed to save engine state: " + e.getMessage(), e);
        }
    }

    @Override
    public void loadState(Path path) throws EngineLoadException {
        try {
            EngineImpl loaded = EngineIO.load(path); // שימוש במתודה הקיימת

            this.xmlPath = loaded.xmlPath;
            this.program = loaded.program;
            this.programExecutor = loaded.programExecutor;
            this.executionHistory = loaded.executionHistory;
        } catch (IOException | ClassNotFoundException e) {
            throw new EngineLoadException("Failed to load engine state: " + e.getMessage(), e);
        }
    }


    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(xmlPath != null ? xmlPath.toString() : null);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        String pathStr = (String) in.readObject();
        this.xmlPath = pathStr != null ? Path.of(pathStr) : null;
    }
}
