package engine;

import dto.DebugDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface Engine {

    void loadProgram(Path Path) throws EngineLoadException;

    ProgramDTO getProgramToDisplay();  // throws if no program loaded
    ProgramDTO getExpandedProgramToDisplay(int degree);
    ProgramExecutorDTO getProgramToDisplayAfterRun();
    List<ProgramExecutorDTO> getHistoryToDisplay();
    ProgramDTO getExpandedProgramDTO(String programName, int degree);
    int getMaxDegree() throws EngineLoadException;  // per current program
    int getMaxDegree(String programName) throws EngineLoadException;
    void calculateExpansionForAllPrograms();
    int getNumberOfInputVariables();
    int getNumberOfInputVariables(String operationName);
    void runProgram(int expandLevel, Long... inputs);
    void runProgram(String operationName, int expandLevel, Long... inputs);
    void initializeDebugger(String programName, int degree, List<Long> inputs);
    DebugDTO getProgramAfterStepOver();
    DebugDTO getProgramAfterResume(List<Boolean> breakPoints) throws InterruptedException;
    DebugDTO getProgramAfterStepBack();
    DebugDTO getInitSnapshot();
    void stopDebugPress();
    List<String> getAllFunctionsNames();
    Map<String, String> getAllUserStringToFunctionName();

    void saveState(Path path) throws EngineLoadException;
    void loadState(Path path) throws EngineLoadException;
}
