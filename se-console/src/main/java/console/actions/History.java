package console.actions;

import console.menu.MenuActionable;
import dto.ExecutionHistoryDTO;

import java.util.Scanner;

public class History implements MenuActionable {
    @Override
    public ExecutionHistoryDTO startAction(Scanner scanner, ExecutionHistoryDTO executionHistoryDTO) {
        return executionHistoryDTO;

    }
}
