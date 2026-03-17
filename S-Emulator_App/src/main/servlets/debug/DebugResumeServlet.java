package main.servlets.debug;

import com.google.gson.reflect.TypeToken;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.service.debug.DebugSessionManager;
import main.utils.ServletUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.utils.Constants.GSON_INSTANCE;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "DebugResumeServlet", urlPatterns = "/debug/resume")
public class DebugResumeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        String sessionId = request.getParameter("sessionId");
        if (sessionId == null || sessionId.isBlank()) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing sessionId parameter");
            return;
        }

        DebugSessionManager manager = DebugSessionManager.getInstance();

        if (manager.getSession(sessionId) == null) {
            writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown sessionId");
            return;
        }

        List<Boolean> breakpoints;
        try {
            Type listType = new TypeToken<List<Boolean>>() {}.getType();
            breakpoints = GSON_INSTANCE.fromJson(request.getReader(), listType);
            if (breakpoints == null) breakpoints = List.of();
        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid breakpoints JSON: " + e.getMessage());
            return;
        }

        try {
            String resumeId = manager.submitResume(sessionId, breakpoints, engine);

            Map<String, String> result = new HashMap<>();
            result.put("resumeId", resumeId);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(GSON_INSTANCE.toJson(result));

        } catch (IllegalArgumentException e) {
            writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
