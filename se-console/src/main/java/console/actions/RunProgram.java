package console.actions;

import console.menu.MenuActionable;
import console.validator.Validator;
import engine.Engine;

import java.util.List;
import java.util.Scanner;

public class RunProgram implements MenuActionable {

    @Override
    public void startAction(Scanner scanner, Engine engine) {

        System.out.println("Max degree of loaded program: " + engine.getMaxDegree());

        int choosenDegree = 0;
        if (engine.getMaxDegree() != 0) {
            System.out.print("Please enter degree for this run: ");
            choosenDegree = Validator.getValidateUserPath(scanner, engine);
        } else {
            System.out.print("The program cannot be increased because its maximum level is 0.");
        }


        Long[] inputs = new Long[0];
        engine.displayUsedInputVariables();
        if (engine.getNumberOfInputVariables() != 0) {
            System.out.print("Please enter inputs values separated by commas: ");
            inputs = Validator.getValidateProgramInputs(scanner, engine);
        }

        System.out.print("----------------------------");

        engine.runProgram(choosenDegree, inputs);

        engine.displayProgramAfterRun();
    }
}
