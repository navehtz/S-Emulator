package main.servlets.execution;

import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.utils.ServletUtils;

import java.io.IOException;

import static main.utils.Constants.PROGRAM_NAME_QUERY_PARAM;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "MaxDegreeServlet", urlPatterns = "/max-degree")
public class MaxDegreeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");
        try {
            String programName = request.getParameter(PROGRAM_NAME_QUERY_PARAM);
            if (programName == null || programName.isEmpty()) {
                writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Missing program name");
                return;
            }

            int maxDegree = engine.getMaxDegree(programName);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(String.valueOf(maxDegree));
        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while fetching max degree: " + e.getMessage());
        }
    }
}
