package engine;

import operation.Operation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ProgramRegistry {
    private final Map<String, Operation> programsByName = new HashMap<>();

    public void register(Operation op) {
        programsByName.put(op.getName(), op);
    }

    public void registerAll(Map<String, Operation> m) {
        programsByName.putAll(m);
    }

    public Operation getByName(String name) {
        Operation operation = programsByName.get(name);
        if (operation == null) {
            throw new IllegalArgumentException("No program with name: " + name);
        }
        return operation;
    }

    public Collection<Operation> allPrograms() {
        return programsByName.values();
    }

    public void clear() {
        programsByName.clear();
    }
}
