package main.servlets.debug;

import dto.execution.DebugResponseDTO;
import engine.Engine;
import exceptions.CreditsException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.service.debug.DebugSessionManager;
import main.utils.ServletUtils;

import java.io.IOException;

import static main.utils.Constants.GSON_INSTANCE;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "DebugStepServlet", urlPatterns = "/debug/step")
public class DebugStepServlet extends HttpServlet {

    private static final String ACTION_OVER = "over";
    private static final String ACTION_BACK = "back";
    private static final String ACTION_STOP = "stop";

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

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing action parameter");
            return;
        }

        DebugSessionManager manager = DebugSessionManager.getInstance();

        // Validate session exists
        if (manager.getSession(sessionId) == null) {
            writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Unknown sessionId");
            return;
        }

        try {
            DebugResponseDTO result;
            switch (action.toLowerCase()) {
                case ACTION_OVER -> result = manager.stepOver(sessionId, engine);
                case ACTION_BACK -> result = manager.stepBack(sessionId, engine);
                case ACTION_STOP -> result = manager.stop(sessionId, engine);
                default -> {
                    writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Invalid action: '" + action + "'. Must be over/back/stop");
                    return;
                }
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(GSON_INSTANCE.toJson(result));

        } catch (CreditsException e) {
            writeJsonError(response, 402, "Out of credits: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
