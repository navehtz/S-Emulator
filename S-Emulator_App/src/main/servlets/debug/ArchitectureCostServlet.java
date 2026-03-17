package main.servlets.debug;

import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.utils.ServletUtils;

import java.io.IOException;

import static main.utils.Constants.*;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "ArchitectureCostServlet", urlPatterns = "/architectureCost")
public class ArchitectureCostServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        String architecture = request.getParameter(CHOSEN_ARCHITECTURE_STR_QUERY_PARAM);
        if (!validateArchitecture(architecture, response)) return;

        try {
            int cost = engine.getArchitectureCost(architecture);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(String.valueOf(cost));
        } catch (IllegalArgumentException e) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Unknown architecture: " + architecture);
        }
    }
}
