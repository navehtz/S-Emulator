package console.actions;

import console.menu.MenuActionable;
import engine.Engine;
import exceptions.EngineLoadException;

import java.util.Scanner;

public class DisplayProgram implements MenuActionable {
    @Override
    public void startAction(Scanner scanner, Engine engine) {
        engine.displayProgram();
    }
}
