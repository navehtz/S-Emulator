package engine.imp;

import engine.*;

import java.nio.file.Path;
import java.util.List;

public class EngineImp implements Engine {
    @Override
    public boolean hasProgram() {
        return false;
    }

    @Override
    public void loadFromXml(Path xmlPath) throws EngineLoadException {

    }

    @Override
    public String getProgramDisplay() {
        return "";
    }

    @Override
    public RunResult run() throws EngineRunException {
        return null;
    }

    @Override
    public List<RunSummary> getHistory() {
        return List.of();
    }
}
