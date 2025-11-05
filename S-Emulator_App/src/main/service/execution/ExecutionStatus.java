package main.service.execution;

import dto.execution.ExecutionStatusDTO;
import dto.execution.RunState;

import java.util.concurrent.atomic.AtomicReference;

public final class ExecutionStatus {
    private final AtomicReference<Snapshot> executionStatusSnapshot =
            new AtomicReference<>(new Snapshot(RunState.PENDING, 0, ""));

    public record Snapshot(RunState state, int progressPercent, String message) {}

    public Snapshot getAtomicSnapshot() {
        return executionStatusSnapshot.get();
    }

    public void setState(RunState newState) {
        executionStatusSnapshot.updateAndGet(
                snapshot -> new Snapshot(newState, snapshot.progressPercent, snapshot.message));
    }

    public void setProgressPercent(int newProgressPercent) {
        int percent = Math.max(0, Math.min(100, newProgressPercent));
        executionStatusSnapshot.updateAndGet(
                snapshot -> new Snapshot(snapshot.state, percent, snapshot.message));
    }

    public void setMessage(String newMessage) {
        executionStatusSnapshot.updateAndGet(
                snapshot -> new Snapshot(snapshot.state, snapshot.progressPercent, newMessage));
    }

    public ExecutionStatusDTO toDTO(String id, String programName, String username) {
        Snapshot snapshot = executionStatusSnapshot.get();
        return new ExecutionStatusDTO(id, programName, username, snapshot.state, snapshot.progressPercent, snapshot.message);
    }
}
