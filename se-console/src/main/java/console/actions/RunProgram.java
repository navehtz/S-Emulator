package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import engine.Engine;
import exceptions.EngineLoadException;

import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class RunProgram implements MenuActionable {

    @Override
    public void startAction(Scanner scanner, Engine engine) throws EngineLoadException {
        printTitle("Run Loaded Program");
        int degree = 0;

        if (engine.getMaxDegree() == 0 ) {
            System.out.println("The program must run on degree zero because its maximum degree is already 0");
        } else {
            System.out.println("Max degree of loaded program: " + engine.getMaxDegree());
            System.out.print("Please enter degree for this run: ");
            degree = Validator.getValidateUserInputForDegree(scanner, engine);
        }

        Long[] inputs = new Long[0];
        engine.displayUsedInputVariables();
        if (engine.getNumberOfInputVariables() != 0) {
            System.out.print("Please enter inputs values separated by commas: ");
            inputs = Validator.getValidateProgramInputs(scanner, engine);
        }

        engine.runProgram(degree, inputs);

        engine.displayProgramAfterRun();
    }
}
