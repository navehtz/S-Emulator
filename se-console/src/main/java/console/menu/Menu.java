package console.menu;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public interface Menu {
    void show(Scanner scanner) throws IOException;
    void addSubItem(MenuItem item);
    void printCurrentMenu();
    List<MenuItem> getSubItems();
    boolean isLeaf();
}
