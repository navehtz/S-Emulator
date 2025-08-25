package engine;

import exceptions.EngineLoadException;

import java.nio.file.Path;

public interface Engine {

    void loadProgram(Path path) throws EngineLoadException;

    void displayProgram();

    int getMaxDegree();
    void displayExpandedProgram(int degree);


    void displayUsedInputVariables();
    void runProgram(int degree, Long... inputs);
    int getNumberOfInputVariables();
    void displayProgramAfterRun();
    void displayHistory();

}
