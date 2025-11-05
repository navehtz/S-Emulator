package main.servlets.execution;

import dto.execution.ProgramDTO;
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

@WebServlet(name = "ProgramDTOServlet", urlPatterns = "/program-dto")
public class ProgramDTOServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!validateUserSession(request, response)) return;

        Engine engine = ServletUtils.getEngine(getServletContext());
        if (!validateEngineNotNull(engine, response)) return;

        response.setContentType("application/json");

        try {
            String programName = request.getParameter(PROGRAM_NAME_QUERY_PARAM);
            if (programName == null || programName.isEmpty()) {
                writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Missing program name");
                return;
            }

            String degreeStr = request.getParameter("degree");
            int degree = 0;

            if (degreeStr != null && !degreeStr.isBlank()) {
                try {
                    degree = Integer.parseInt(degreeStr);
                    if (degree < 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException exception) {
                    writeJsonError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Degree must be a non-negative integer");
                    return;
                }
            }

            ProgramDTO programDTO =  engine.getExpandedProgramDTO(programName, degree);

            if (programDTO == null) {
                writeJsonError(response, HttpServletResponse.SC_NOT_FOUND, "Program not found");
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(GSON_INSTANCE.toJson(programDTO));

        } catch (Exception e) {
            writeJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Server error while fetching program data: " + e.getMessage());
        }
    }
}
