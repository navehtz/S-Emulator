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

@WebServlet(name = "AverageCyclesServlet", urlPatterns = "/averageCycles")
public class AverageCyclesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        String programName = request.getParameter(PROGRAM_NAME_QUERY_PARAM);
        if (!validateProgramName(programName, response)) return;

        String degreeStr = request.getParameter(DEGREE_QUERY_PARAM);
        int degree;
        try {
            degree = Integer.parseInt(degreeStr);
        } catch (NumberFormatException e) {
            writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid degree parameter");
            return;
        }

        double avgCycles = engine.getAverageCycles(programName, degree);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.getWriter().write(String.valueOf(avgCycles));
    }
}
