package engine;

import debug.Debug;
import debug.DebugImpl;
import dto.DebugDTO;
import dto.InstructionsDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;
import execution.ProgramExecutorImpl;
import function.Function;
import function.FunctionDisplayResolver;
import history.ExecutionHistory;
import execution.ProgramExecutor;
import history.ExecutionHistoryImpl;
import operation.Operation;
import loader.XmlProgramLoader;
import operation.OperationView;
import program.Program;
import users.UserManager;
import variable.Variable;

import java.io.*;
import java.nio.file.Path;
import java.util.*;


public class EngineImpl implements Engine, Serializable {
    private final ProgramRegistry registry = new ProgramRegistry();
    private Map<String, OperationView> loadedOperations = new HashMap<>();
    private Operation mainProgram;
    private ProgramExecutor programExecutor;
    //private ExecutionHistory executionHistory;
    private final Map<String, ExecutionHistory> programToExecutionHistory = new HashMap<>();
    private transient Path xmlPath;
    private transient Debug debug;
    private final Map<String, Map<Integer, OperationView>> nameAndDegreeToProgram = new HashMap<>();
    private UserManager userManager = new UserManager();


    @Override
    public void loadProgram(Path xmlPath) throws EngineLoadException {
        this.xmlPath = xmlPath;

        XmlProgramLoader loader = new XmlProgramLoader();
        LoadResult loadResult = loader.loadAll(xmlPath);

        for (OperationView operation : loadResult.allOperationsByName.values()) {
            operation.validateProgram();
            operation.initialize();
        }

        this.mainProgram = loadResult.getMainProgram();
        this.registry.clear();
        this.loadedOperations = new HashMap<>(loadResult.allOperationsByName);
        this.registry.registerAll(loadResult.allOperationsByName);
        for (OperationView opView : loadedOperations.values()) {
            opView.setRegistry(this.registry);
        }
        this.mainProgram.setRegistry(this.registry);

        FunctionDisplayResolver.populateDisplayNames(loadResult.getAllByName().values(), registry);

        calculateExpansionForAllPrograms();
    }

    @Override
    public void loadProgram(InputStream inputStream) throws EngineLoadException {

        XmlProgramLoader loader = new XmlProgramLoader();
        LoadResult loadResult = loader.loadAll(inputStream);

        for (OperationView operation : loadResult.allOperationsByName.values()) {
            operation.validateProgram();
            operation.initialize();
        }

        this.mainProgram = loadResult.getMainProgram();
        this.registry.clear();
        this.loadedOperations = new HashMap<>(loadResult.allOperationsByName);
        this.registry.registerAll(loadResult.allOperationsByName);
        for (OperationView opView : loadedOperations.values()) {
            opView.setRegistry(this.registry);
        }
        this.mainProgram.setRegistry(this.registry);

        FunctionDisplayResolver.populateDisplayNames(loadResult.getAllByName().values(), registry);

        calculateExpansionForAllPrograms();
    }

    @Override
    public void runProgram(int degree, Long... inputs) {
        Map<String, OperationView> clonedOperations = new HashMap<>();
        for (Map.Entry<String, OperationView> entry : loadedOperations.entrySet()) {
            clonedOperations.put(entry.getKey(), entry.getValue().deepClone());
        }

        ProgramRegistry runRegistry = new ProgramRegistry();
        runRegistry.registerAll(clonedOperations);

        for (OperationView op : clonedOperations.values()) {
            op.setRegistry(runRegistry);
        }

        for (OperationView operation : clonedOperations.values()) {
            operation.expandProgram(degree);
        }


        OperationView mainClone = clonedOperations.get(mainProgram.getName());

        programExecutor = new ProgramExecutorImpl(mainClone, runRegistry);

        programExecutor.run(degree, inputs);
        ExecutionHistory executionHistory = new ExecutionHistoryImpl();
        executionHistory.addProgramToHistory(programExecutor);
        programToExecutionHistory.putIfAbsent(mainProgram.getName(), executionHistory);

    }

    @Override
    public int getNumberOfInputVariables() {
        return mainProgram.getInputVariables().size();
    }

    @Override
    public ProgramDTO getProgramToDisplay() {
        return buildProgramDTO(mainProgram);
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
    public List<ProgramExecutorDTO> getHistoryToDisplayByProgramName(String programName) {
        List<ProgramExecutorDTO> out = new ArrayList<>();
        ExecutionHistory executionHistory = programToExecutionHistory.get(programName);
        if (executionHistory == null) return out;

        for(ProgramExecutor programExecutorItem : executionHistory.getProgramsExecutions()) {

            ProgramDTO programDTO = buildProgramDTO(programExecutorItem.getProgram());

            out.add(new ProgramExecutorDTO(
                    programDTO,
                    programExecutorItem.getVariablesToValuesSorted(),
                    programExecutorItem.getVariableValue(Variable.RESULT),
                    programExecutorItem.getTotalCyclesOfProgram(),
                    programExecutorItem.getRunDegree(),
                    programExecutorItem.getInputsValuesOfUser()
            ));

        }

        return out;
    }

    @Override
    public int getMaxDegree() throws EngineLoadException {
        return this.nameAndDegreeToProgram.get(mainProgram.getName()).size() - 1; // The size of the map is the max degree
    }

    @Override
    public ProgramDTO getExpandedProgramDTO(String programName, int degree) {
        return buildProgramDTO(getExpandedProgram(programName, degree));
    }

    private OperationView getExpandedProgram(String programName, int degree) {
        Map<Integer, OperationView> degreeMap = this.nameAndDegreeToProgram.get(programName);
        if (degreeMap == null) {
            throw new IllegalArgumentException("Program not found: " + programName);
        }

        OperationView expandedProgram = degreeMap.get(degree);
        if (expandedProgram == null) {
            throw new IllegalArgumentException("Degree " + degree + " not found for program: " + programName);
        }

        expandedProgram.setRegistry(this.registry);

        return expandedProgram;
    }

    @Override
    public ProgramDTO getExpandedProgramToDisplay(int degree) {
        Operation deepCopyOfProgram = mainProgram.deepClone();
        deepCopyOfProgram.expandProgram(degree);

        return buildProgramDTO(deepCopyOfProgram);
    }

    @Override
    public void calculateExpansionForAllPrograms() {
        this.nameAndDegreeToProgram.put(
                mainProgram.getName(),
                mainProgram.calculateDegreeToProgram()
        );

        for (OperationView function : registry.allPrograms()) {
            this.nameAndDegreeToProgram.put(
                    function.getName(),
                    function.calculateDegreeToProgram()
            );
        }
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
            this.mainProgram = loaded.mainProgram;
            this.programExecutor = loaded.programExecutor;
            this.programToExecutionHistory.putIfAbsent(mainProgram.getName(), loaded.programToExecutionHistory.get(mainProgram.getName()));
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

    @Override
    public List<String> getAllFunctionsNames() {
        return registry.allPrograms().stream().map(OperationView::getName).toList();
    }

    @Override
    public int getMaxDegree(String programName) throws EngineLoadException {
        Map<Integer, OperationView> byDegree = nameAndDegreeToProgram.get(programName);
        if (byDegree == null || byDegree.isEmpty()) {
            throw new EngineLoadException("Unknown program/function: " + programName);
        }
        return byDegree.size() - 1;
    }

    @Override
    public Map<String, String> getAllUserStringToFunctionName() {
        Map<String, String> out = new LinkedHashMap<>();
        for (OperationView operation : loadedOperations.values()) {
            if (operation instanceof Function function) {
                String functionName = function.getName();
                String userString = function.getUserString();
                out.putIfAbsent(userString, functionName);
            } else if (operation instanceof Program) {
                String programName = operation.getName();
                out.putIfAbsent(programName, programName);
            }
        }
        return out;
    }

    @Override
    public void runProgram(String operationName, int degree, Long... inputs) {
        // clone all operations for this run
        Map<String, OperationView> cloned = new HashMap<>();
        for (var e : loadedOperations.entrySet()) cloned.put(e.getKey(), e.getValue().deepClone());
        // expand all
        for (OperationView op : cloned.values()) op.expandProgram(degree);

        ProgramRegistry runRegistry = new ProgramRegistry();
        runRegistry.registerAll(cloned);

        OperationView target = cloned.get(operationName);
        if (target == null) throw new IllegalArgumentException("Program not found: " + operationName);

        programExecutor = new ProgramExecutorImpl(target, runRegistry);
        programExecutor.run(degree, inputs);
        ExecutionHistory executionHistory = programToExecutionHistory
                .computeIfAbsent(operationName, k -> new ExecutionHistoryImpl());
        executionHistory.addProgramToHistory(programExecutor);
    }

    @Override
    public int getNumberOfInputVariables(String operationName) {
        OperationView op = loadedOperations.get(operationName);
        if (op == null) throw new IllegalArgumentException("Program not found: " + operationName);
        return op.getInputVariables().size();
    }

    @Override
    public void initializeDebugger(String programName, int degree, List<Long> inputs) {
        Map<String, OperationView> cloned = new HashMap<>();
        for (var entry : loadedOperations.entrySet()) {
            cloned.put(entry.getKey(), entry.getValue().deepClone());
        }

        for (OperationView op : cloned.values()) {
            op.setRegistry(registry);
            op.expandProgram(degree);
        }

        ProgramRegistry runRegistry = new ProgramRegistry();
        runRegistry.registerAll(cloned);
        for (OperationView op : cloned.values()) {
            op.setRegistry(runRegistry);
        }

        OperationView target = cloned.get(programName);
        if (target == null) {
            throw new IllegalArgumentException("Program not found: " + programName);
        }

        this.debug = new DebugImpl(target, runRegistry, degree, inputs != null ? inputs : List.of() );
    }

    @Override
    public DebugDTO getProgramAfterStepOver() {
        DebugDTO debugDTO = debug.stepOver();    // Step Over
        return debugDTO;
    }

    private void addDebugResultToHistoryMap(DebugDTO debugDTO) {
    }

    @Override
    public DebugDTO getProgramAfterResume(List<Boolean> breakPoints) throws InterruptedException {
        DebugDTO debugDTO = debug.resume(breakPoints);  // Resume

        return debugDTO;
    }

    @Override
    public DebugDTO getProgramAfterStepBack() {
        return debug.stepBack();
    }

    @Override
    public void stopDebugPress() {
        DebugDTO debugDTO = debug.stop();
    }

    @Override
    public DebugDTO getInitSnapshot() {
        if (debug == null) throw new IllegalStateException("Debugger not initialized");
        return debug.init();
    }

    public UserManager getUserManager() {
        return userManager;
    }
}
