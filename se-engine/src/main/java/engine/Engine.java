package engine;

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
    void runProgram(int expandLevel, Long... inputs);
    List<String> getAllFunctionsNames();
    Map<String, String> getAllUserStringToFunctionName();

    void saveState(Path path) throws EngineLoadException;
    void loadState(Path path) throws EngineLoadException;
}
