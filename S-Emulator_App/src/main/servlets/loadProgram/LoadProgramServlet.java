package main.servlets.loadProgram;

import com.google.gson.Gson;
import dto.execution.ProgramDTO;
import engine.Engine;
import exceptions.EngineLoadException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.utils.ServletUtils;
import main.utils.SessionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import static main.utils.Constants.GSON_INSTANCE;

@WebServlet(name = "LoadProgramServlet", urlPatterns = "/loadProgram")
public class LoadProgramServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String mainProgramName = null;
        try {
            mainProgramName = processRequest(request, response);
        } catch (EngineLoadException e) {
            throw new RuntimeException(e);
        }

        ProgramDTO baseProgram =ServletUtils.getEngine(getServletContext()).getProgramByNameToDisplay(mainProgramName);

        String jsonResponse = GSON_INSTANCE.toJson(baseProgram);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);

        try (PrintWriter out = response.getWriter()) {
            out.println(jsonResponse);
            out.flush();
        }
    }

    private String processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, EngineLoadException {
        byte[] xmlBytes = request.getInputStream().readAllBytes();
        try (var in = new ByteArrayInputStream(xmlBytes)) {
            Engine engine = ServletUtils.getEngine(getServletContext());
            String username = SessionUtils.getUsername(request);

            String mainProgramName = engine.loadProgram(in, username);
            return mainProgramName;
        }
        catch (EngineLoadException e) {
            System.out.println("Error loading program");
            throw new EngineLoadException("Error loading program", e);
        }
    }
}
