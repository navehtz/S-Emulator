package main.servlets.users;

import dto.dashboard.UserDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.utils.ServletUtils;
import main.utils.SessionUtils;
import users.UserManager;

import java.io.IOException;

import static main.utils.Constants.CREDITS_AMOUNT_QUERY_PARAM;
import static main.utils.Constants.GSON_INSTANCE;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "CreditsServlet", urlPatterns = "/credits")
public class CreditsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        String username = SessionUtils.getUsername(request);
        UserManager userManager = engine.getUserManager();
        UserDTO user = userManager.getUserByName(username);

        if (user == null) {
            writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(GSON_INSTANCE.toJson(user));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        String username = SessionUtils.getUsername(request);
        String newCreditsStr = request.getParameter(CREDITS_AMOUNT_QUERY_PARAM);

        if (newCreditsStr == null || newCreditsStr.isBlank()) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing new credits amount");
            return;
        }

        long newCredits;
        try {
            newCredits = Long.parseLong(newCreditsStr);
        } catch (NumberFormatException exception) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "New credits amount must be a non-negative integer");
            return;
        }
        if (newCredits <= 0) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "New credits amount must be a non-negative integer");
            return;
        }

        UserManager userManager = engine.getUserManager();
        UserDTO user = userManager.getUserByName(username);

        synchronized (userManager) {
            if (!userManager.isUserExists(username)) {
                writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
                return;
            }
            userManager.addCredits(username, newCredits);
        }

        UserDTO updatedUser = userManager.getUserByName(username);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(GSON_INSTANCE.toJson(updatedUser));
    }
}
