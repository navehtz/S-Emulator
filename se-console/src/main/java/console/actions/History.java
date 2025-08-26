package console.actions;

import console.menu.MenuActionable;
import engine.Engine;

import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class History implements MenuActionable {
    @Override
    public void startAction(Scanner scanner, Engine engine) {
        printTitle("Present History");

        engine.displayHistory();
    }
}
