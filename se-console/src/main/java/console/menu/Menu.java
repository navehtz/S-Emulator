package console.menu;

import engine.Engine;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public interface Menu {
    void show(Scanner scanner, Engine engine) throws IOException;
    void addSubItem(MenuItem item);
    void printCurrentMenu();
    boolean isLeaf();
}
