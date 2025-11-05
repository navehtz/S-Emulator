package main.servlets.dashboard;

import dto.dashboard.AvailableProgramDTO;
import dto.execution.ProgramDTO;
import engine.Engine;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.utils.ServletUtils;
import main.utils.SessionUtils;
import operation.OperationView;
import program.Program;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static main.utils.Constants.GSON_INSTANCE;

@WebServlet(name = "AvailableProgramsServlet", urlPatterns = "/programs")
public class AvailableProgramsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Engine engine = ServletUtils.getEngine(getServletContext());

        List<AvailableProgramDTO> outProgramsList = new ArrayList<>();

        for (OperationView operation : engine.getAllOperations()) {
            if (!(operation instanceof Program)) continue;

            String programName = operation.getName();
            int numInstructions = 0;
            ProgramDTO programDTO = engine.getProgramByNameToDisplay(programName);
            if (programDTO != null && programDTO.instructions() != null && programDTO.instructions().programInstructionsDTOList() != null) {
                numInstructions = programDTO.instructions().programInstructionsDTOList().size();
            }

            int maxDegree = 0;
            try { maxDegree = engine.getMaxDegree(programName); } catch (Exception ignored) {}

            int numExecutions = 0;
            try {
                var historyOfProgram = engine.getHistoryToDisplayByProgramName(programName);
                if (historyOfProgram != null) {
                    numExecutions = historyOfProgram.size();
                }
            } catch (Exception ignored) {}

            //TODO: Exposure
            String uploadedBy = operation.getUserUploaded();
            int averageCreditCost = 0;

            outProgramsList.add(new AvailableProgramDTO(
                    programName,
                    uploadedBy,
                    numInstructions,
                    maxDegree,
                    numExecutions,
                    averageCreditCost
            ));
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        GSON_INSTANCE.toJson(outProgramsList, response.getWriter());
    }
}
