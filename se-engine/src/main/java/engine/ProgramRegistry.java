package engine;

import operation.Operation;
import operation.OperationView;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ProgramRegistry implements Serializable {
    private final Map<String, OperationView> programsByName = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void register(OperationView op) {
        lock.writeLock().lock();
        try {
            programsByName.put(op.getName(), op);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void registerAll(Map<String, OperationView> m) {
        lock.writeLock().lock();
        try {
            programsByName.putAll(m);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public OperationView getProgramByName(String name) {
        lock.readLock().lock();
        try {
            OperationView operation = programsByName.get(name);
            if (operation == null) {
                throw new IllegalArgumentException("No program with name: " + name);
            }
            else {
                return operation;
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public Collection<OperationView> getAllPrograms() {
        lock.readLock().lock();
        try {
            return programsByName.values();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            programsByName.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Map<String, OperationView> getAllProgramsByName() {
        lock.readLock().lock();
        try {
            return new HashMap<>(programsByName);
        } finally {
            lock.readLock().unlock();
        }
    }
}
