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
    private MenuActionable actionToExecute;
    private Engine engine;
    Scanner scanner;


    public MenuItem(String title) {
        this.title = title;
    }

    public MenuItem(String commandLine, MenuActionable action, Engine engine) {
        this.commandLine = commandLine;
        this.actionToExecute = action;
        this.engine = engine;
    }

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
            catch (IllegalArgumentException  e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private int getValidateUserChoice(Scanner scanner) {
        String input = scanner.nextLine().trim();
        int max = subItems.size() + 1;

        if (input.isEmpty()) {
            throw new IllegalArgumentException("Invalid input. Choice cannot be empty.");
        }

        final int choice;
        try {
            choice = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Invalid input, numbers only between 1 and " + max + ". Got: " + input
            );
        }

        if (choice < 1 || choice > max) {
            throw new IllegalArgumentException(
                    "Choice out of range, enter a number between 1 and " + max + ". Got: " + choice
            );
        }

        return choice;
    }

    private boolean handleChoice(int userChoice) {
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
            }
        }

        return backOrExitPressed;
    }

    @Override
    public void printCurrentMenu() {
        int numberOfSubItems = this.subItems.size();
        printTitle("Menu");

        for (int i = 0; i < numberOfSubItems; i++) {
            System.out.printf("%d. %s%n", i + 1, subItems.get(i).commandLine);
        }

        System.out.printf("%d. %s%n", numberOfSubItems + 1, "Exit");
        System.out.printf("Please enter your choice (1-%d): ", subItems.size() + 1);
    }

    public static void printTitle(String title) {
        if (title == null) title = "";
        int totalWidth = 60;
        int inner = Math.max(0, totalWidth - title.length() - 2);
        int left = inner / 2;
        int right = inner - left;
        System.out.printf("%s %s %s%n", "=".repeat(left), title, "=".repeat(right));
    }

    private void execute(MenuItem selectedItem) throws EngineLoadException {
        selectedItem.actionToExecute.startAction(scanner, engine);
        System.out.println();
    }
}
