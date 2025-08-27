package console.menu;

import engine.Engine;

import java.io.IOException;
import java.util.Scanner;

public interface Menu {
    boolean show(Scanner scanner, Engine engine) throws IOException;
    void addSubItem(MenuItem item);
    void printCurrentMenu();
    boolean isLeaf();
}
