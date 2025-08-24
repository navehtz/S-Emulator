package console.menu;

import dto.ExecutionHistoryDTO;
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
    private ExecutionHistoryDTO executionHistory;


    public MenuItem(String title) {
        this.title = title;
    }

    public MenuItem(String commandLine, MenuActionable action, ExecutionHistoryDTO executionHistoryDTO) {
        this.commandLine = commandLine;
        this.actionToExecute = action;
        this.executionHistory = executionHistoryDTO;
    }

    public MenuItem(String CommandLine, String title, MenuActionable action, ExecutionHistoryDTO executionHistoryDTO) {
        this.commandLine = CommandLine;
        this.title = title;
        this.actionToExecute = action;
        this.executionHistory = executionHistoryDTO;
    }

    @Override
    public void show(Scanner scanner) {
        boolean isExitPressed = false;
        this.scanner = scanner;

        while (!isExitPressed) {
            printCurrentMenu();

            try {
                int userChoice =  getValidateUserChoice(scanner);
                isExitPressed = handleChoice(userChoice);
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("Press Enter to try again...");
            }
        }
    }

    private int getValidateUserChoice(Scanner scanner) {
        String input = scanner.nextLine();
        try {
            int choice = Integer.parseInt(input.trim());

            if (choice < 0 || choice > subItems.size()) {
                throw new IllegalArgumentException("Invalid input.");
            }

            return choice;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input.");
        }
    }

    @Override
    public List<MenuItem> getSubItems() {
        return subItems;
    }

    private boolean handleChoice(int userChoice) throws EngineLoadException {
        boolean backOrExitPressed = false;

        if (userChoice == 0) {
            backOrExitPressed = true;
        }
        else if (userChoice >= 1 && userChoice <= subItems.size()) {
            MenuItem selectedItem = subItems.get(userChoice - 1);

            if (!selectedItem.isLeaf()) {
                selectedItem.show(scanner);
            } else {
                clearScreen();
                selectedItem.actionToExecute.startAction(scanner, executionHistory);
                System.out.println();
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
            }
        } else {    // never reach hear
            System.out.println("Invalid input.");
        }

        return backOrExitPressed;
    }

    @Override
    public void addSubItem(MenuItem newSubItem) {
        this.subItems.add(newSubItem);
    }

    @Override
    public void printCurrentMenu() {
        int numberOfSubItems = this.subItems.size();

        clearScreen();

        System.out.println(title);
        System.out.println("-".repeat(title.length() + 6));

        for (int i = 0; i < numberOfSubItems; i++) {
            System.out.printf("%d. %s%n", i + 1, subItems.get(i).commandLine);
        }

        System.out.printf("0. %s%n", "Exit");
        System.out.printf("Please enter your choice (1-%d) or 0 to Exit", subItems.size());
        System.out.println(System.lineSeparator());
    }

    @Override
    public boolean isLeaf() {
        return subItems.isEmpty();
    }

    private static void clearScreen() {
        System.out.print("\u001B[H\u001B[2J");
        System.out.flush();
    }
}
