package main.service.debug;

import debug.Debug;
import dto.execution.DebugDTO;
import dto.execution.DebugResponseDTO;
import dto.execution.RunState;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

public class DebugSession {

    private final String sessionId;
    private final Debug debugInstance;
    private final String userName;
    private final String programName;

    // For async resume tracking
    private final AtomicReference<RunState> resumeState = new AtomicReference<>(RunState.PENDING);
    private final AtomicReference<DebugResponseDTO> resumeResult = new AtomicReference<>();
    private final AtomicReference<String> resumeMessage = new AtomicReference<>("");
    private final AtomicReference<Future<?>> resumeFuture = new AtomicReference<>();

    // Last known snapshot (used to compute cyclesDiff for stepBack deduction)
    private volatile DebugDTO lastSnapshot;

    public DebugSession(String sessionId, Debug debugInstance, String userName, String programName) {
        this.sessionId = sessionId;
        this.debugInstance = debugInstance;
        this.userName = userName;
        this.programName = programName;
    }

    public String getSessionId() { return sessionId; }
    public Debug getDebugInstance() { return debugInstance; }
    public String getUserName() { return userName; }
    public String getProgramName() { return programName; }

    public DebugDTO getLastSnapshot() { return lastSnapshot; }
    public void setLastSnapshot(DebugDTO snap) { this.lastSnapshot = snap; }

    public RunState getResumeState() { return resumeState.get(); }
    public void setResumeState(RunState state) { resumeState.set(state); }

    public DebugResponseDTO getResumeResult() { return resumeResult.get(); }
    public void setResumeResult(DebugResponseDTO result) { resumeResult.set(result); }

    public String getResumeMessage() { return resumeMessage.get(); }
    public void setResumeMessage(String msg) { resumeMessage.set(msg); }

    public Future<?> getResumeFuture() { return resumeFuture.get(); }
    public void setResumeFuture(Future<?> future) { resumeFuture.set(future); }
}
