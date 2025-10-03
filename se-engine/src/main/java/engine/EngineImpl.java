package engine;

import dto.InstructionsDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;
import execution.ProgramExecutorImpl;
import function.FunctionDisplayResolver;
import history.ExecutionHistory;
import execution.ProgramExecutor;
import history.ExecutionHistoryImpl;
import operation.Operation;
import loader.XmlProgramLoader;
import operation.OperationView;
import program.Program;
import variable.Variable;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EngineImpl implements Engine, Serializable {
    private final ProgramRegistry registry = new ProgramRegistry();
    private Map<String, OperationView> loadedOperations = new HashMap<>();
    private Operation program;
    private ProgramExecutor programExecutor;
    private ExecutionHistory executionHistory;
    private transient Path xmlPath;


    @Override
    public void loadProgram(Path xmlPath) throws EngineLoadException {
        this.xmlPath = xmlPath;
        //Operation newProgram;

        XmlProgramLoader loader = new XmlProgramLoader();
        LoadResult loadResult = loader.loadAll(xmlPath);

        for (OperationView operation : loadResult.allOperationsByName.values()) {
            operation.validateProgram();
            operation.initialize();
        }

        this.program = loadResult.getMainProgram();
        this.registry.clear();
        //loadResult.getAllByName().values().forEach(registry::register);
        this.loadedOperations = new HashMap<>(loadResult.allOperationsByName);
        this.registry.registerAll(loadResult.allOperationsByName);
        if (this.program instanceof Program programImpl) {
            programImpl.setRegistry(registry);
        }

        FunctionDisplayResolver.populateDisplayNames(loadResult.getAllByName().values(), registry);

        executionHistory = new ExecutionHistoryImpl();
    }

    @Override
    public void runProgram(int degree, Long... inputs) {
        Map<String, OperationView> clonedOperations = new HashMap<>();
        for (Map.Entry<String, OperationView> entry : loadedOperations.entrySet()) {
            clonedOperations.put(entry.getKey(), entry.getValue().deepClone());
        }

        for (OperationView operation : clonedOperations.values()) {
            operation.expandProgram(degree);
        }

        ProgramRegistry runRegistry = new ProgramRegistry();
        runRegistry.registerAll(clonedOperations);

        OperationView mainClone = clonedOperations.get(program.getName());

        programExecutor = new ProgramExecutorImpl(mainClone, runRegistry);

        programExecutor.run(degree, inputs);
        executionHistory.addProgramToHistory(programExecutor);

//        Operation deepCopyOfProgram = program.deepClone();
//        deepCopyOfProgram.expandProgram(degree);
//
//        programExecutor = new ProgramExecutorImpl(deepCopyOfProgram);
//
//        programExecutor.run(degree, inputs);
//        executionHistory.addProgramToHistory(programExecutor);
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
        List<ProgramExecutorDTO> historyToDisplay = new ArrayList<>();

        for(ProgramExecutor programExecutorItem : executionHistory.getProgramsExecutions()) {

            ProgramDTO programDTO = buildProgramDTO(program);

            ProgramExecutorDTO programExecutorDTO = new ProgramExecutorDTO(programDTO,
                    programExecutorItem.getVariablesToValuesSorted(),
                    programExecutorItem.getVariableValue(Variable.RESULT),
                    programExecutorItem.getTotalCyclesOfProgram(),
                    programExecutorItem.getRunDegree(),
                    programExecutorItem.getInputsValuesOfUser()
            );

            historyToDisplay.add(programExecutorDTO);
        }

        return historyToDisplay;
    }

    @Override
    public int getMaxDegree() throws EngineLoadException {
        if(program == null) {
            throw new EngineLoadException("Program not loaded before asking for max degree");
        }

        return program.calculateProgramMaxDegree();
    }

    @Override
    public ProgramDTO getExpandedProgramToDisplay(int degree) {
        Operation deepCopyOfProgram = program.deepClone();
        deepCopyOfProgram.expandProgram(degree);

        return buildProgramDTO(deepCopyOfProgram);
    }

    private ProgramDTO buildProgramDTO(OperationView program) {
        InstructionsDTO instructionsDTO = new InstructionsDTO(program.getInstructionDTOList());
        List<String> allInputsWithSerial = new ArrayList<>(program.getInputAndWorkVariablesSortedBySerial().stream().map(Variable::getRepresentation).toList());
        allInputsWithSerial.addFirst(Variable.RESULT.getRepresentation());

        return new ProgramDTO(
                program.getName(),
                program.getOrderedLabelsExitLastStr(),
                program.getInputVariablesSortedStr(),
                instructionsDTO,
                program.getExpandedProgram(),
                allInputsWithSerial
        );
    }

    @Override
    public void saveState(Path path) throws EngineLoadException {
        try {
            EngineIO.save(this, path);
        } catch (IOException e) {
            throw new EngineLoadException("Failed to save engine state: " + e.getMessage(), e);
        }
    }

    @Override
    public void loadState(Path path) throws EngineLoadException {
        try {
            EngineImpl loaded = EngineIO.load(path);

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
