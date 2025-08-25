package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import engine.Engine;
import exceptions.EngineLoadException;
import history.ExecutionHistory;
import history.ExecutionHistoryImpl;
import execution.ProgramExecutorImpl;
import program.Program;
import loader.XmlProgramLoader;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Scanner;



public class LoadFile implements MenuActionable {

    @Override
    public void startAction(Scanner scanner, Engine engine) throws EngineLoadException {

        System.out.print("Please enter full path to your file: ");

        Path xmlPath = Validator.getValidateUserPath(scanner);
        engine.loadProgram(xmlPath);

        System.out.println("Successfully loaded the file");
    }
}
