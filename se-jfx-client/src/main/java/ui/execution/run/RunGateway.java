package ui.execution.run;

import dto.execution.ExecutionStatusDTO;
import dto.execution.ProgramDTO;
import dto.execution.ProgramExecutorDTO;

import java.io.IOException;
import java.util.List;

public interface RunGateway {
    ProgramDTO fetchExpanded(String programName, int degree) throws IOException;   // already have; useful if you need inputs here
    List<String> fetchRequiredInputs(String programName, int degree) throws IOException;

    String submitRun(String programName, String architecture, int degree, List<Long> inputs) throws IOException;

    ExecutionStatusDTO getStatus(String runId) throws IOException;

    ProgramExecutorDTO fetchResult(String programName, String runId) throws IOException;
}
