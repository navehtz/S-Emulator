package main.servlets.debug;

import debug.Debug;
import dto.execution.DebugDTO;
import dto.execution.DebugResponseDTO;
import dto.execution.DebugStartRequestDTO;
import engine.Engine;
import exceptions.CreditsException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.service.debug.DebugSession;
import main.service.debug.DebugSessionManager;
import main.utils.ServletUtils;
import main.utils.SessionUtils;

import java.io.IOException;
import java.util.List;

import static main.utils.Constants.GSON_INSTANCE;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "StartDebugServlet", urlPatterns = "/debug/start")
public class StartDebugServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;
        String username = SessionUtils.getUsername(request);

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        DebugStartRequestDTO startRequest;
        try {
            startRequest = GSON_INSTANCE.fromJson(request.getReader(), DebugStartRequestDTO.class);
        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON body: " + e.getMessage());
            return;
        }

        if (startRequest == null || startRequest.programName() == null || startRequest.programName().isBlank()) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing programName");
            return;
        }
        if (!validateArchitecture(startRequest.architecture(), response)) return;
        if (!validateDegree(startRequest.degree(), response)) return;

        List<Long> inputs = startRequest.inputs() != null ? startRequest.inputs() : List.of();

        try {
            Debug debug = engine.createDebug(
                    startRequest.programName(),
                    startRequest.architecture(),
                    startRequest.degree(),
                    username,
                    inputs
            );

            DebugDTO initSnap = debug.init();

            String sessionId = DebugSessionManager.getInstance().createSession(debug, username,
                    startRequest.programName(), startRequest.architecture(), inputs);

            DebugSession session = DebugSessionManager.getInstance().getSession(sessionId);
            session.setLastSnapshot(initSnap);

            long credits = engine.getUserManager().getUserByName(username).currentCredits();
            DebugResponseDTO responseDTO = new DebugResponseDTO(sessionId, initSnap, credits);

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(GSON_INSTANCE.toJson(responseDTO));

        } catch (CreditsException e) {
            writeJsonError(response, 402, "Out of credits: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
