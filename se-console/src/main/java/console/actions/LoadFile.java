package console.actions;

import console.menu.MenuActionable;
import dto.ExecutionHistoryDTO;
import exceptions.EngineLoadException;
import history.ExecutionHistory;
import history.ExecutionHistoryImpl;
import execution.ProgramExecutorImpl;
import program.Program;
import xmlStructure.loader.XmlProgramLoader;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Scanner;


public class LoadFile implements MenuActionable {
    private Scanner scanner;

    @Override
    public ExecutionHistoryDTO startAction(Scanner scanner, ExecutionHistoryDTO executionHistoryDTO) throws EngineLoadException {
        this.scanner = scanner;

        System.out.println("Please enter full path to your file");
        Path xmlPath =  getValidateUserPath();

        // TODO: להחזיק פה רק DTO
        XmlProgramLoader xmlProgramLoader = new XmlProgramLoader();
        Program program = xmlProgramLoader.load(xmlPath);
        program.validateProgram();
        program.initialize();

        ProgramExecutorImpl programExecutor = new ProgramExecutorImpl(program);
        ExecutionHistory executionHistory = new ExecutionHistoryImpl();
        executionHistory.addProgramToHistory(programExecutor);

        return executionHistoryDTO;
    }

    private Path getValidateUserPath() {
        String input = scanner.nextLine();

        if (input.isEmpty()) {
            throw new IllegalArgumentException("Path is empty.");
        }

        try {
            Path path = Path.of(input).toAbsolutePath().normalize();
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("Path does not exist: " + path);
            }

            return path;
        }
        catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid path syntax: " + input, e);
        }
    }
}
