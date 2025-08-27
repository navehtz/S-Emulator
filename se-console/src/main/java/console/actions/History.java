package console.actions;

import console.menu.MenuActionable;
import dto.ProgramExecutorDTO;
import engine.Engine;

import java.util.List;
import java.util.Scanner;

import static console.menu.MenuItem.printTitle;

public class History implements MenuActionable {
    @Override
    public void startAction(Scanner scanner, Engine engine) {
        printTitle("Present History");

        List<ProgramExecutorDTO> programExecutorDTOList = engine.getHistoryToDisplay();

        if (!programExecutorDTOList.isEmpty()) {
            displayHistory(programExecutorDTOList);
        } else {
            System.out.println("No history has been set in this program. Run the program first.");
        }
    }

    private void displayHistory(List<ProgramExecutorDTO> programExecutorDTOList) {
        int i = 1;

        for (ProgramExecutorDTO programExecutorDTO : programExecutorDTOList) {
            System.out.println("#" + i);
            System.out.println("Run degree: " + programExecutorDTO.getDegree());

            var inputs = programExecutorDTO.getInputsValuesOfUser();
            String inputsDisplay = (inputs == null || inputs.isEmpty())
                    ? "no input from user"
                    : inputs.stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.joining(", "));

            System.out.println("Inputs values: " + inputsDisplay);
            System.out.println("Result: " + programExecutorDTO.getResult());
            System.out.println("Cycles: " + programExecutorDTO.getTotalCycles());
            System.out.println();

            i++;
        }
    }

/*    @Override
    public ExecutionHistoryDTO displayExecutionHistory() {

        StringBuilder executionHistory = new StringBuilder();

        for(int i = 0; i < programExecutorsHistory.size(); i++) {
            ProgramExecutor programExecutor = programExecutorsHistory.get(i);

            executionHistory.append("Run number #").append(i + 1).append(System.lineSeparator());
            executionHistory.append("Run degree: ").append(programExecutor.getRunDegree()).append(System.lineSeparator());
            executionHistory.append("Inputs values: ").append(System.lineSeparator());

            for(int j = 0; j < programExecutor.getInputsValues().size(); j++) {
                long inputValue = programExecutor.getInputsValues().get(j);
                executionHistory.append("x").append(j + 1).append(" = ").append(inputValue).append(System.lineSeparator());
            }

            executionHistory.append("Result: ").append(programExecutor.getResultValue()).append(System.lineSeparator());
            executionHistory.append("Cycles: ").append(programExecutor.getTotalCyclesOfProgram());

            executionHistory.append(System.lineSeparator()).append(System.lineSeparator());
        }

        return executionHistory.toString();
    }*/
}
