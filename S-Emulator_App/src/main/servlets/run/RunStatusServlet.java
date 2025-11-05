package main.servlets.run;

import dto.execution.ExecutionStatusDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.service.execution.ProgramExecutionManager;

import java.io.IOException;

import static main.utils.Constants.GSON_INSTANCE;
import static main.utils.Constants.RUN_ID_QUERY_PARAM;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "RunStatusServlet", urlPatterns = "/runStatus")
public class RunStatusServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        String runId = request.getParameter(RUN_ID_QUERY_PARAM);
        if (!validateRunIdParam(runId, response)) return;

        ExecutionStatusDTO executionStatusDTO = ProgramExecutionManager.getInstance().getExecutionStatus(runId);
        if (executionStatusDTO == null) {
            writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown runId");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(GSON_INSTANCE.toJson(executionStatusDTO));
    }
}
