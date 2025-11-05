package main.servlets.users;

import dto.dashboard.UserDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.utils.ServletUtils;
import main.utils.SessionUtils;

import java.io.IOException;

import static main.utils.Constants.GSON_INSTANCE;

@WebServlet(name = "CreditsBalanceServlet", urlPatterns = "/credits/balance")
public class CreditsBalanceServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"error\":\"not logged in\"}");
            return;
        }

        Engine engine = ServletUtils.getEngine(getServletContext());
        UserDTO user = engine.getUserManager().getUserByName(username);
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"error\":\"user not found\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(GSON_INSTANCE.toJson(user));

    }
}
