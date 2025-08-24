package console.menu;

import dto.ExecutionHistoryDTO;
import exceptions.EngineLoadException;

import java.util.Scanner;

public interface MenuActionable {

    ExecutionHistoryDTO startAction(Scanner scanner, ExecutionHistoryDTO executionHistoryDTO) throws EngineLoadException;
}
