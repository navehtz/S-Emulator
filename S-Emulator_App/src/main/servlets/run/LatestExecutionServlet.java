package main.servlets.run;

import dto.execution.ProgramExecutorDTO;
import engine.Engine;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.utils.ServletUtils;

import java.io.IOException;
import java.util.List;

import static main.utils.Constants.GSON_INSTANCE;
import static main.utils.ValidationUtils.*;

@WebServlet(name = "LatestExecutionServlet", urlPatterns = "/latestExecution")
public class LatestExecutionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        String programName = request.getParameter("programName");
        if (!validateProgramName(programName, response)) return;

        try {
            List<ProgramExecutorDTO> executionsHistory = engine.getHistoryToDisplayByProgramName(programName);

            if (executionsHistory.isEmpty()) {
                writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "No executions for program");
                return;
            }

            ProgramExecutorDTO latest = executionsHistory.getLast();

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(GSON_INSTANCE.toJson(latest));
        }  catch (Exception e) {
        e.printStackTrace();
    }
    }
}
