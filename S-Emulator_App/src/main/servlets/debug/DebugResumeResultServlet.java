package main.servlets.debug;

import dto.execution.DebugResponseDTO;
import dto.execution.ExecutionStatusDTO;
import dto.execution.RunState;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.service.debug.DebugSessionManager;

import java.io.IOException;

import static main.utils.Constants.GSON_INSTANCE;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "DebugResumeResultServlet", urlPatterns = "/debug/resumeResult")
public class DebugResumeResultServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        String resumeId = request.getParameter("resumeId");
        if (resumeId == null || resumeId.isBlank()) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing resumeId parameter");
            return;
        }

        DebugSessionManager manager = DebugSessionManager.getInstance();

        ExecutionStatusDTO status = manager.getResumeStatus(resumeId);
        if (status == null) {
            writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown resumeId");
            return;
        }

        if (status.state() == RunState.ERROR) {
            writeJsonError(response, HttpServletResponse.SC_CONFLICT,
                    "Resume execution failed: " + status.message());
            return;
        }

        if (status.state() != RunState.DONE && status.state() != RunState.OUT_OF_CREDITS) {
            writeJsonError(response, HttpServletResponse.SC_CONFLICT, "Resume not finished yet");
            return;
        }

        DebugResponseDTO result = manager.getResumeResult(resumeId);
        if (result == null) {
            writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "No resume result available");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(GSON_INSTANCE.toJson(result));
    }
}
