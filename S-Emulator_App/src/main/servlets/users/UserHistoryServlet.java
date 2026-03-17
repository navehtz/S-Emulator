package main.servlets.users;

import dto.dashboard.UserHistoryRowDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.utils.Constants;
import main.utils.ServletUtils;
import main.utils.SessionUtils;

import java.io.IOException;
import java.util.List;

import static main.utils.Constants.GSON_INSTANCE;

@WebServlet(name = "UserHistoryServlet", urlPatterns = "/userHistory")
public class UserHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String sessionUsername = SessionUtils.getUsername(request);
        if (sessionUsername == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            try (var out = response.getWriter()) {
                out.print("{\"error\":\"not logged in\"}");
            }
            return;
        }

        String targetUser = request.getParameter(Constants.USER_NAME_QUERY_PARAM);
        if (targetUser == null || targetUser.isBlank()) {
            targetUser = sessionUsername;
        }

        Engine engine = ServletUtils.getEngine(getServletContext());
        List<UserHistoryRowDTO> historyOfUser = engine.getUserHistory(targetUser);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        try (var out = response.getWriter()) {
            out.print(GSON_INSTANCE.toJson(historyOfUser));
        }
    }
}