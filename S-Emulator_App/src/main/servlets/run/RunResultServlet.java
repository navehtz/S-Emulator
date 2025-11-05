package main.servlets.run;

import dto.execution.ProgramExecutorDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.utils.ServletUtils;

import java.io.IOException;

import static main.utils.Constants.GSON_INSTANCE;
import static main.utils.Constants.PROGRAM_NAME_QUERY_PARAM;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "RunResultServlet", urlPatterns = "/runResult")
public class RunResultServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;
        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        // Optional: verify programName exists; not strictly required since engine holds last run result globally
        String programName = request.getParameter(PROGRAM_NAME_QUERY_PARAM);
        if (!validateProgramName(programName, response)) return;

        try {
            ProgramExecutorDTO programExecutorDTO = engine.getProgramToDisplayAfterRun();
            if (programExecutorDTO == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(GSON_INSTANCE.toJson("No run result available"));
                return;
            }
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(GSON_INSTANCE.toJson(programExecutorDTO));
        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Failed to fetch run result: " + e.getMessage());
        }
    }
}
