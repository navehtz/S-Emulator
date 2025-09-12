package engine;

import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.List;

public interface Engine {

    void loadProgram(Path Path) throws EngineLoadException;

    ProgramDTO getProgramToDisplay();  // throws if no program loaded
    ProgramDTO getExpandedProgramToDisplay(int degree);
    ProgramExecutorDTO getProgramToDisplayAfterRun();
    List<ProgramExecutorDTO> getHistoryToDisplay();

    int getMaxDegree() throws EngineLoadException;  // per current program
    int getNumberOfInputVariables();
    void runProgram(int expandLevel, Long... inputs);

    void saveState(Path path) throws EngineLoadException;
    void loadState(Path path) throws EngineLoadException;
}
