package ui.execution.debug;

import dto.execution.DebugResponseDTO;
import dto.execution.ExecutionStatusDTO;

import java.io.IOException;
import java.util.List;

public interface DebugGateway {

    DebugResponseDTO startDebug(String programName, String architecture, int degree, List<Long> inputs) throws IOException;

    DebugResponseDTO stepOver(String sessionId) throws IOException;

    DebugResponseDTO stepBack(String sessionId) throws IOException;

    DebugResponseDTO stop(String sessionId) throws IOException;

    String submitResume(String sessionId, List<Boolean> breakpoints) throws IOException;

    ExecutionStatusDTO getResumeStatus(String resumeId) throws IOException;

    DebugResponseDTO getResumeResult(String resumeId) throws IOException;

    double getAverageCycles(String programName, int degree) throws IOException;

    int getArchitectureCost(String architecture) throws IOException;

    long getCreditsBalance() throws IOException;

    List<String> fetchRequiredInputs(String programName, int degree) throws IOException;
}
