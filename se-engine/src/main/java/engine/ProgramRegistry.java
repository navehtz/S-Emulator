package engine;

import operation.Operation;
import operation.OperationView;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProgramRegistry implements Serializable {
    private final Map<String, OperationView> programsByName = new HashMap<>();

    public void register(Operation op) {
        programsByName.put(op.getName(), op);
    }

    public void registerAll(Map<String, OperationView> m) {
        programsByName.putAll(m);
    }

    public OperationView getProgramByName(String name) {
        OperationView operation = programsByName.get(name);
        if (operation == null) {
            throw new IllegalArgumentException("No program with name: " + name);
        }
        return operation;
    }

    public Collection<OperationView> allPrograms() {
        return programsByName.values();
    }

    public void clear() {
        programsByName.clear();
    }
}
