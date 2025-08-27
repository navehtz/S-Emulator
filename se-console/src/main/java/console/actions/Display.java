package console.actions;

import console.menu.MenuActionable;
import dto.InstructionsDTO;
import dto.ProgramDTO;
import engine.Engine;
import engine.EngineImpl;

import java.util.List;
import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class Display implements MenuActionable {

    @Override
    public void startAction(Scanner scanner, Engine engine) {
        printTitle("Display Program");
        ProgramDTO programDTO = engine.getProgramToDisplay();
        displayProgram(programDTO);
    }

    public static void displayProgram(ProgramDTO programDTO) {
        List<String> variablesInputInProgram = programDTO.getInputVariables();
        List<String> labels = java.util.Optional.ofNullable(programDTO.getLabelsStr()).orElse(java.util.List.of());

        String labelsDisplay = labels.isEmpty()
                ? "no labels in program"
                : String.join(", ", labels);

        String display = String.format(
                "Name: %s%n" +
                "Inputs: %s%n" +
                "Labels: %s%n" +
                "Instructions:%n%s",
                programDTO.getProgramName(),
                String.join(", ", variablesInputInProgram),
                labelsDisplay,
                programRepresentation(programDTO.getInstructions())
        );

        System.out.println(display);
    }

    public static String programRepresentation(InstructionsDTO instructionsDTO) {
        return String.join(System.lineSeparator(), instructionsDTO.getProgramInstructionsStr());
    }
}
