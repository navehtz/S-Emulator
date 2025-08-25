package console;

import console.actions.*;
import console.menu.MainMenu;
import console.menu.MenuItem;
import engine.Engine;
import engine.EngineImpl;

import java.util.Scanner;

public class UIManager {

    private final MenuItem menu;

    private final LoadFile loadFile;
    private final DisplayProgram displayProgram;
    private final Expand expand;
    private final RunProgram runProgram;
    private final History history;
    private final Scanner scanner;

    private final Engine engine;


    public UIManager() {
        this.scanner = new Scanner(System.in);
        engine = new EngineImpl();

        this.loadFile = new LoadFile();
        this.displayProgram = new DisplayProgram();
        this.expand = new Expand();
        this.runProgram = new RunProgram();
        this.history = new History();

        this.menu = buildMenu();
    }

    private MainMenu buildMenu() {
        MainMenu mainMenu = new MainMenu("Menu");

        // First submenu
        MenuItem loadFirstFileItem = new MenuItem("Load New File", loadFile, this.engine);

        // Second submenu
        MenuItem loadNextFileItem   = new MenuItem("Load Next File", loadFile, engine);
        MenuItem displayProgramItem = new MenuItem("display Program", this.displayProgram, engine);
        MenuItem expandItem         = new MenuItem("Expand Loaded Program", this.expand, engine);
        MenuItem runFileItem        = new MenuItem("Run File", this.runProgram, engine);
        MenuItem historyItem        = new MenuItem("Show History", this.history, engine);

        mainMenu.addSubItem(loadFirstFileItem);

        loadFirstFileItem.addSubItem(loadNextFileItem);
        loadFirstFileItem.addSubItem(displayProgramItem);
        loadFirstFileItem.addSubItem(expandItem);
        loadFirstFileItem.addSubItem(runFileItem);
        loadFirstFileItem.addSubItem(historyItem);

        return mainMenu;
    }

    public void run() {
        try {
            menu.show(scanner, engine);
        }
        catch (Exception e) {
            // io exception
        }
    }

}
