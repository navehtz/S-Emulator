package consoleUI;

import consoleUI.actions.*;
import consoleUI.menu.MainMenu;
import consoleUI.menu.MenuItem;
import engine.Engine;
import engine.EngineImpl;

import java.util.Scanner;

public class ConsoleUI {

    private final MenuItem menu;

    private final LoadFile loadFile;
    private final Display display;
    private final Expand expand;
    private final RunProgram runProgram;
    private final History history;
    private final SaveState saveState;
    private final LoadState loadState;

    private static final Scanner scanner = new Scanner(System.in);
    private static final Engine engine = new EngineImpl();

    public ConsoleUI() {
        this.loadFile = new LoadFile();
        this.display = new Display();
        this.expand = new Expand();
        this.runProgram = new RunProgram();
        this.history = new History();
        this.saveState = new SaveState();
        this.loadState = new LoadState();

        this.menu = buildMenu();
    }

    private MainMenu buildMenu() {
        MainMenu mainMenu = new MainMenu("Menu");

        // First submenu
        MenuItem loadFirstFileItem = new MenuItem("Load New File", loadFile, engine);

        // Second submenu
        MenuItem loadNextFileItem   = new MenuItem("Load Next File", loadFile, engine);
        MenuItem displayProgramItem = new MenuItem("display Program", this.display, engine);
        MenuItem expandItem         = new MenuItem("Expand Loaded Program", this.expand, engine);
        MenuItem runFileItem        = new MenuItem("Run File", this.runProgram, engine);
        MenuItem historyItem        = new MenuItem("Show History", this.history, engine);
        MenuItem saveState   = new MenuItem("Save State", this.saveState, engine);
        MenuItem LoadState   = new MenuItem("Load State", this.loadState, engine);

        mainMenu.addSubItem(loadFirstFileItem);

        loadFirstFileItem.addSubItem(loadNextFileItem);
        loadFirstFileItem.addSubItem(displayProgramItem);
        loadFirstFileItem.addSubItem(expandItem);
        loadFirstFileItem.addSubItem(runFileItem);
        loadFirstFileItem.addSubItem(historyItem);
        loadFirstFileItem.addSubItem(saveState);
        loadFirstFileItem.addSubItem(LoadState);

        return mainMenu;
    }


    public void run() {
        try {
            menu.show(scanner, engine);
        }
        catch (Exception e) {   // i/o exception. not suppose to reach hear
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


