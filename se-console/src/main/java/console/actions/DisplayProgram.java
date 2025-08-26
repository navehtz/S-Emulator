package console.actions;

import console.menu.MenuActionable;
import engine.Engine;
import exceptions.EngineLoadException;

import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class DisplayProgram implements MenuActionable {
    @Override
    public void startAction(Scanner scanner, Engine engine) {
        printTitle("Display Program");
        engine.displayProgram();
        //לקבל חזרה ליד אובייקט DTO
        // programDTO
        // לשלוף מידע מהDTO
        // List<>
    }
}
