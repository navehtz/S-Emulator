package main.servlets.run;

import dto.execution.ExecutionStatusDTO;
import dto.execution.ProgramExecutorDTO;
import dto.execution.RunState;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.service.execution.ExecutionStatus;
import main.service.execution.ProgramExecutionManager;
import main.utils.ServletUtils;
import main.utils.SessionUtils;

import java.io.IOException;

import static main.utils.Constants.*;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "RunResultServlet", urlPatterns = "/runResult")
public class RunResultServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;
        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        // Optional: verify programName exists; not strictly required since engine holds last run result globally
        String programName = request.getParameter(PROGRAM_NAME_QUERY_PARAM);
        if (!validateProgramName(programName, response)) return;
        String runId = request.getParameter(RUN_ID_QUERY_PARAM);
        if (!validateRunIdParam(runId, response)) return;

        ProgramExecutionManager manager = ProgramExecutionManager.getInstance();
        ExecutionStatusDTO status = manager.getExecutionStatus(runId);

        if (status == null) {
            writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown runId. No matching execution found for this ID");
            return;
        }

        if (status.state() == RunState.ERROR) {
            // Execution has completed but ended in failure
            writeJsonError(response, HttpServletResponse.SC_CONFLICT,
                    "Program execution failed: " + status.message());
            return;
        }

        if (status.state() != RunState.DONE) {
            // Execution is still running or pending
            writeJsonError(response, HttpServletResponse.SC_CONFLICT,
                    "Program not finished yet. Run is still in progress");
            return;
        }

        try {
            ProgramExecutorDTO programExecutorDTO = engine.getProgramToDisplayAfterRun(status.userName());
            if (programExecutorDTO == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(GSON_INSTANCE.toJson("No run result available"));
                return;
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(GSON_INSTANCE.toJson(programExecutorDTO));
        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to fetch run result: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
