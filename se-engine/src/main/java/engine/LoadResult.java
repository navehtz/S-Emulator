package engine;

import operation.Operation;

import java.util.Map;
import java.util.Objects;

public class LoadResult {

    private final Operation mainProgram;               // or Operation
    final Map<String, Operation> allOperationsByName; // name -> program/function

    public LoadResult(Operation mainProgram, Map<String, Operation> allByName) {
        this.mainProgram = Objects.requireNonNull(mainProgram, "main");
        this.allOperationsByName = Objects.requireNonNull(allByName, "allByName");

        if (!allByName.containsKey(mainProgram.getName())) {
            throw new IllegalArgumentException("allByName must contain the main program under its name");
        }
    }

    public Operation getMainProgram() {
        return mainProgram;
    }

    public Map<String, Operation> getAllByName() {
        return allOperationsByName;
    }

}
