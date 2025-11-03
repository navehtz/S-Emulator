package main.servlets.users;

import com.google.gson.Gson;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.utils.ServletUtils;
import main.utils.SessionUtils;
import users.UserManager;

import java.io.IOException;

@WebServlet(name = "UsersServlet", urlPatterns = "/users")
public class UsersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String username = SessionUtils.getUsername(request);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            try (var out = response.getWriter()) {
                out.print("{\"error\":\"not logged in\"}");
            }
            return;
        }

        Engine engine = ServletUtils.getEngine(getServletContext());
        UserManager userManager = engine.getUserManager();

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");

        Gson gson = new Gson();

        try (var out = response.getWriter()) {
            out.print(gson.toJson(userManager.getUsers()));
        } catch (Exception e) {
            System.out.println("Error getting users " + e.getMessage());
        }
    }
}
