package engine;

import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import exceptions.EngineLoadException;

import java.nio.file.Path;
import java.util.List;

public interface Engine {

    void loadProgram(Path path) throws EngineLoadException;

    ProgramDTO getProgramToDisplay();
    ProgramDTO getExpandedProgramToDisplay(int degree) throws EngineLoadException;
    ProgramExecutorDTO getProgramToDisplayAfterRun();
    List<ProgramExecutorDTO> getHistoryToDisplay();

    int getMaxDegree() throws EngineLoadException;
    int getNumberOfInputVariables();
    void runProgram(int degree, Long... inputs) throws EngineLoadException;

    void saveState(Path path) throws EngineLoadException;
    void loadState(Path path) throws EngineLoadException;
}
