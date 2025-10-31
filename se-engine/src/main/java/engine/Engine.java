package engine;

import dto.execution.DebugDTO;
import dto.execution.ProgramDTO;
import dto.execution.ProgramExecutorDTO;
import exceptions.EngineLoadException;
import users.UserManager;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface Engine {

    void loadProgram(Path Path) throws EngineLoadException;
    void loadProgram(InputStream inputStream) throws EngineLoadException;

    ProgramDTO getProgramToDisplay();  // throws if no program loaded
    ProgramDTO getExpandedProgramToDisplay(int degree);
    ProgramExecutorDTO getProgramToDisplayAfterRun();
    List<ProgramExecutorDTO> getHistoryToDisplayByProgramName(String programName);
    ProgramDTO getExpandedProgramDTO(String programName, int degree);
    int getMaxDegree() throws EngineLoadException;  // per current program
    int getMaxDegree(String programName) throws EngineLoadException;
    void calculateExpansionForAllPrograms();
    int getNumberOfInputVariables();
    int getNumberOfInputVariables(String operationName);
    //void runProgram(int expandLevel, Long... inputs);
    void runProgram(String operationName, int expandLevel, Long... inputs);
    void initializeDebugger(String programName, int degree, List<Long> inputs);
    DebugDTO getProgramAfterStepOver();
    DebugDTO getProgramAfterResume(List<Boolean> breakPoints) throws InterruptedException;
    DebugDTO getProgramAfterStepBack();
    DebugDTO getInitSnapshot();
    void stopDebugPress();
    List<String> getAllFunctionsNames();
    Map<String, String> getAllUserStringToFunctionName();
    UserManager getUserManager();

    void saveState(Path path) throws EngineLoadException;
    void loadState(Path path) throws EngineLoadException;
}
