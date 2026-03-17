package main.servlets.debug;

import dto.execution.ExecutionStatusDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.service.debug.DebugSessionManager;

import java.io.IOException;

import static main.utils.Constants.GSON_INSTANCE;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "DebugResumeStatusServlet", urlPatterns = "/debug/resumeStatus")
public class DebugResumeStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        String resumeId = request.getParameter("resumeId");
        if (resumeId == null || resumeId.isBlank()) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing resumeId parameter");
            return;
        }

        ExecutionStatusDTO status = DebugSessionManager.getInstance().getResumeStatus(resumeId);
        if (status == null) {
            writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown resumeId");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(GSON_INSTANCE.toJson(status));
    }
}
