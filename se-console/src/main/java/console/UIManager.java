package console;

import console.actions.Expand;
import console.actions.History;
import console.actions.LoadFile;
import console.actions.RunFile;
import console.menu.MainMenu;
import console.menu.MenuItem;
import dto.ExecutionHistoryDTO;

import java.util.Scanner;

public class UIManager {

    private final MenuItem menu;

    private final LoadFile loadFile;
    private final RunFile runFile;
    private final Expand expand;
    private final History history;
    private final Scanner scanner;

    private final ExecutionHistoryDTO executionHistory;


    public UIManager() {
        this.scanner = new Scanner(System.in);

        this.loadFile = new LoadFile();
        this.runFile = new RunFile();
        this.expand = new Expand();
        this.history = new History();

        this.menu = buildMenu();

        executionHistory = new ExecutionHistoryDTO();
    }

    private MainMenu buildMenu() {
        MainMenu mainMenu = new MainMenu("Menu");

        // First submenu
        MenuItem loadFirstFileItem = new MenuItem("Load New File", "New File Had Loaded Successfully Fully", loadFile, executionHistory);

        // Second submenu
        MenuItem loadNextFileItem = new MenuItem("Load Next File", loadFile, executionHistory);
        MenuItem runFileItem      = new MenuItem("Run File", this.runFile, executionHistory);
        MenuItem expandItem       = new MenuItem("Expand Loaded Program", this.expand, executionHistory);
        MenuItem historyItem      = new MenuItem("Show History", this.history, executionHistory);

        mainMenu.addSubItem(loadFirstFileItem);

        loadFirstFileItem.addSubItem(loadNextFileItem);
        loadFirstFileItem.addSubItem(runFileItem);
        loadFirstFileItem.addSubItem(expandItem);
        loadFirstFileItem.addSubItem(historyItem);

        return mainMenu;
    }

    public void run() {
        try {
            menu.show(scanner);
        }
        catch (Exception e) {
            // io exception
        }
    }

}
