package engine;

import engine.exceptions.EngineLoadException;
import engine.exceptions.EngineRunException;

import java.nio.file.Path;
import java.util.List;

public interface Engine {
    boolean hasProgram();
    void loadFromXml(Path xmlPath) throws EngineLoadException;
    String getProgramDisplay();
    RunResult run() throws EngineRunException;
    List<RunSummary> getHistory();
}

