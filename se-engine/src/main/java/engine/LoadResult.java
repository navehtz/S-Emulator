package engine;

import operation.Operation;
import operation.OperationView;

import java.util.Map;
import java.util.Objects;

public class LoadResult {

    private final Operation mainProgram;               // or Operation
    final Map<String, OperationView> allOperationsByName; // name -> program/function

    public LoadResult(Operation mainProgram, Map<String, OperationView> allByName) {
        this.mainProgram = Objects.requireNonNull(mainProgram, "main");
        this.allOperationsByName = Objects.requireNonNull(allByName, "allByName");

        if (!allByName.containsKey(mainProgram.getName())) {
            throw new IllegalArgumentException("allByName must contain the main program under its name");
        }
    }

    public Operation getMainProgram() {
        return mainProgram;
    }

    public Map<String, OperationView> getAllByName() {
        return allOperationsByName;
    }

}
