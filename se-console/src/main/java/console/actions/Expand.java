package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import dto.ProgramDTO;
import engine.Engine;
import exceptions.EngineLoadException;

import java.util.List;
import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class Expand implements MenuActionable {

    @Override
    public void startAction(Scanner scanner, Engine engine) throws EngineLoadException {
        printTitle("Present Expand Program");

        if (engine.getMaxDegree() == 0) {
            System.out.println("The program cannot be expand because its maximum degree is already 0");
            return;
        }

        System.out.println("Max degree of loaded program: " + engine.getMaxDegree());

        System.out.print("Please enter degree for this run: ");
        int degree = Validator.getValidateDegree(scanner, engine);

        ProgramDTO programDTO = engine.getExpandedProgramToDisplay(degree);

        displayExpandedProgram(programDTO);
    }

    private void displayExpandedProgram(ProgramDTO programDTO) {
        for (List<String> line : programDTO.getExpandedProgram()) {
            System.out.println(String.join(" >>> ", line));
        }
    }
}