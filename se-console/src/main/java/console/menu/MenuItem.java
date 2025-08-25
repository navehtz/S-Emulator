package console.menu;

import engine.Engine;
import exceptions.EngineLoadException;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class MenuItem implements Menu {

    public String title;
    public String commandLine;
    private final List<MenuItem> subItems = new ArrayList<>();
    Scanner scanner;
    private MenuActionable actionToExecute;
    private Engine engine;


    public MenuItem(String title) {
        this.title = title;
    }

    public MenuItem(String commandLine, MenuActionable action, Engine engine) {
        this.commandLine = commandLine;
        this.actionToExecute = action;
        this.engine = engine;
    }

/*    public MenuItem(String CommandLine, String title, MenuActionable action, Engine engine) {
        this.commandLine = CommandLine;
        this.title = title;
        this.actionToExecute = action;
        this.engine = engine;
    }*/

    @Override
    public boolean isLeaf() {
        return subItems.isEmpty();
    }

    @Override
    public void addSubItem(MenuItem newSubItem) {
        this.subItems.add(newSubItem);
    }

    @Override
    public void show(Scanner scanner, Engine engine) {
        boolean isExitPressed = false;
        this.scanner = scanner;
        this.engine = engine;

        while (!isExitPressed) {
            printCurrentMenu();

            try {
                int userChoice =  getValidateUserChoice(scanner);
                isExitPressed = handleChoice(userChoice);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Press Enter to try again...");
                scanner.nextLine();
            }
        }
    }

    private int getValidateUserChoice(Scanner scanner) {
        String input = scanner.nextLine();

        try {
            int choice = Integer.parseInt(input.trim());

            if (choice < 1 || choice > subItems.size() + 1) {
                throw new IllegalArgumentException("Invalid input.");
            }

            return choice;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input.");
        }
    }

    private boolean handleChoice(int userChoice) throws EngineLoadException {
        boolean backOrExitPressed = false;
        int exitNumber = this.subItems.size() + 1;

        if (userChoice == exitNumber) {
            backOrExitPressed = true;
            System.out.println("Good Bye!");
        }
        else {
            MenuItem selectedItem = subItems.get(userChoice - 1);

            try {
                execute(selectedItem);

                if (!selectedItem.isLeaf()) {
                    selectedItem.show(scanner, engine);
                }
            } catch (EngineLoadException e) {
                System.out.println(e.getMessage());
                System.out.println("Press Enter to try again...");
                scanner.nextLine();
            }
        }

  /*      } else {    // never reach hear
            System.out.println("Invalid input.");
        }*/

        return backOrExitPressed;
    }

    @Override
    public void printCurrentMenu() {
        int numberOfSubItems = this.subItems.size();

        System.out.println("============================================");
        for (int i = 0; i < numberOfSubItems; i++) {
            System.out.printf("%d. %s%n", i + 1, subItems.get(i).commandLine);
        }

        System.out.printf("%d. %s%n", numberOfSubItems + 1, "Exit");
        System.out.printf("Please enter your choice (1-%d) or 0 to Exit", subItems.size());
        System.out.println(System.lineSeparator());
    }

    private void execute(MenuItem selectedItem) throws EngineLoadException {
        selectedItem.actionToExecute.startAction(scanner, engine);
        System.out.println();
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }
}
