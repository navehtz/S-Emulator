package engine;

import operation.Operation;
import operation.OperationView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static loader.XmlProgramMapper.normalizeKey;
import static loader.XmlProgramMapper.safeTrim;

public class ProgramRegistry implements Serializable {
    private final Map<String, OperationView> programsByName = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void register(OperationView op) {
        lock.writeLock().lock();
        try {
            programsByName.put(normalizeKey(op.getName()), op);
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
            OperationView operation = programsByName.get(normalizeKey(name));
            if (operation == null) {
                throw new IllegalArgumentException("No program with name: " + name);
            }
            return operation;
        } finally {
            lock.readLock().unlock();
        }
    }

    public Collection<OperationView> getAllPrograms() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(programsByName.values());
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

    public OperationView findProgramByNameOrNull(String name) {
        lock.readLock().lock();
        try {
            return programsByName.get(name);
        } finally {
            lock.readLock().unlock();
        }
    }
}
