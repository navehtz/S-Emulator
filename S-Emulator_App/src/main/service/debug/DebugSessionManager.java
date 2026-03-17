package main.service.debug;

import debug.Debug;
import dto.execution.*;
import engine.Engine;
import exceptions.CreditsException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class DebugSessionManager {

    private static final DebugSessionManager INSTANCE = new DebugSessionManager();
    public static DebugSessionManager getInstance() { return INSTANCE; }

    private final Map<String, DebugSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> resumeIdToSessionId = new ConcurrentHashMap<>();
    private final ExecutorService virtualThreads = Executors.newVirtualThreadPerTaskExecutor();

    private DebugSessionManager() {}

    /**
     * Creates a new debug session and returns its sessionId.
     */
    public String createSession(Debug debug, String userName, String programName,
                                String architecture, List<Long> inputs) {
        String sessionId = UUID.randomUUID().toString();
        DebugSession session = new DebugSession(sessionId, debug, userName, programName, architecture, inputs);
        sessions.put(sessionId, session);
        return sessionId;
    }

    /**
     * Executes stepOver on the session. Returns a DebugResponseDTO with updated credits.
     * Throws CreditsException if the user runs out of credits.
     * Throws IllegalArgumentException if the sessionId is unknown.
     */
    public DebugResponseDTO stepOver(String sessionId, Engine engine) {
        DebugSession session = getSessionOrThrow(sessionId);
        DebugDTO snap = session.getDebugInstance().stepOver();
        session.setLastSnapshot(snap);
        long credits = engine.getUserManager().getUserByName(session.getUserName()).currentCredits();
        return new DebugResponseDTO(sessionId, snap, credits);
    }

    /**
     * Executes stepBack on the session, deducting credits equal to the cycles difference.
     * Returns a DebugResponseDTO with updated credits.
     */
    public DebugResponseDTO stepBack(String sessionId, Engine engine) {
        DebugSession session = getSessionOrThrow(sessionId);
        Debug debug = session.getDebugInstance();

        DebugDTO beforeSnap = session.getLastSnapshot();
        int cyclesBefore = (beforeSnap != null) ? beforeSnap.totalCycles() : 0;

        DebugDTO snap = debug.stepBack();
        session.setLastSnapshot(snap);

        int cyclesAfter = snap.totalCycles();
        long cyclesDiff = (long) cyclesBefore - cyclesAfter;

        if (cyclesDiff > 0) {
            if (!engine.getUserManager().trySubtractCredits(session.getUserName(), cyclesDiff)) {
                long current = engine.getUserManager().getUserByName(session.getUserName()).currentCredits();
                throw new CreditsException(current, cyclesDiff);
            }
        }

        long credits = engine.getUserManager().getUserByName(session.getUserName()).currentCredits();
        return new DebugResponseDTO(sessionId, snap, credits);
    }

    /**
     * Stops the debug session (cancels any in-progress resume), removes the session.
     */
    public DebugResponseDTO stop(String sessionId, Engine engine) {
        DebugSession session = getSessionOrThrow(sessionId);

        // Cancel any in-progress resume
        Future<?> future = session.getResumeFuture();
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }

        DebugDTO snap = session.getDebugInstance().stop();
        long credits = engine.getUserManager().getUserByName(session.getUserName()).currentCredits();

        boolean partial = snap != null && snap.hasMoreInstructions();
        engine.recordDebugHistory(session.getUserName(), session.getDebugInstance(),
                session.getArchitecture(), snap != null ? snap.degree() : 0,
                session.getInputs(), snap, partial);

        sessions.remove(sessionId);
        return new DebugResponseDTO(sessionId, snap, credits);
    }

    /**
     * Submits an async resume. Returns a resumeId that can be polled.
     */
    public String submitResume(String sessionId, List<Boolean> breakpoints, Engine engine) {
        DebugSession session = getSessionOrThrow(sessionId);
        String resumeId = UUID.randomUUID().toString();
        resumeIdToSessionId.put(resumeId, sessionId);

        session.setResumeState(RunState.IN_PROGRESS);
        session.setResumeResult(null);
        session.setResumeMessage("");

        Future<?> future = virtualThreads.submit(() -> {
            try {
                DebugDTO result = session.getDebugInstance().resume(breakpoints);
                session.setLastSnapshot(result);
                long credits = engine.getUserManager().getUserByName(session.getUserName()).currentCredits();
                session.setResumeResult(new DebugResponseDTO(sessionId, result, credits));
                engine.recordDebugHistory(session.getUserName(), session.getDebugInstance(),
                        session.getArchitecture(), result != null ? result.degree() : 0,
                        session.getInputs(), result, false);
                session.setResumeState(RunState.DONE);
            } catch (CreditsException e) {
                DebugDTO partial = session.getDebugInstance().stop();
                if (partial != null) session.setLastSnapshot(partial);
                long credits = engine.getUserManager().getUserByName(session.getUserName()).currentCredits();
                session.setResumeResult(new DebugResponseDTO(sessionId, partial, credits));
                session.setResumeMessage(e.getMessage() != null ? e.getMessage() : "Out of credits");
                engine.recordDebugHistory(session.getUserName(), session.getDebugInstance(),
                        session.getArchitecture(), partial != null ? partial.degree() : 0,
                        session.getInputs(), partial, true);
                session.setResumeState(RunState.OUT_OF_CREDITS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                session.setResumeMessage("Cancelled");
                session.setResumeState(RunState.CANCELLED);
            } catch (Throwable t) {
                session.setResumeMessage(t.getMessage() != null ? t.getMessage() : "Execution error");
                session.setResumeState(RunState.ERROR);
                t.printStackTrace();
            }
        });

        session.setResumeFuture(future);
        return resumeId;
    }

    /**
     * Returns the status of an async resume operation.
     */
    public ExecutionStatusDTO getResumeStatus(String resumeId) {
        String sessionId = resumeIdToSessionId.get(resumeId);
        if (sessionId == null) return null;

        DebugSession session = sessions.get(sessionId);
        if (session == null) return null;

        return new ExecutionStatusDTO(
                resumeId,
                session.getProgramName(),
                session.getUserName(),
                session.getResumeState(),
                0,
                session.getResumeMessage()
        );
    }

    /**
     * Returns the result of a completed async resume.
     */
    public DebugResponseDTO getResumeResult(String resumeId) {
        String sessionId = resumeIdToSessionId.get(resumeId);
        if (sessionId == null) return null;

        DebugSession session = sessions.get(sessionId);
        if (session == null) return null;

        return session.getResumeResult();
    }

    public DebugSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    private DebugSession getSessionOrThrow(String sessionId) {
        DebugSession session = sessions.get(sessionId);
        if (session == null) throw new IllegalArgumentException("Unknown debug sessionId: " + sessionId);
        return session;
    }

    public void shutdown() {
        virtualThreads.shutdownNow();
    }
}
