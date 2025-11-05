package main.servlets.dashboard;

import dto.dashboard.AvailableFunctionDTO;
import dto.execution.ProgramDTO;
import engine.Engine;
import function.Function;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import main.utils.ServletUtils;
import main.utils.SessionUtils;
import operation.OperationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static main.utils.Constants.GSON_INSTANCE;

@WebServlet(name = "AvailableFunctionsServlet", urlPatterns = {"/functions"})
public class AvailableFunctionsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Engine engine = ServletUtils.getEngine(getServletContext());

        List<AvailableFunctionDTO> outFunctionsList = new ArrayList<>();

        for (OperationView operation : engine.getAllOperations()) {
            if (!(operation instanceof Function function)) continue;

            String functionName = function.getName();
            String functionUserString = function.getUserString();

            int numInstructions = 0;
            ProgramDTO functionDTO = engine.getProgramByNameToDisplay(functionName);
            if (functionDTO != null && functionDTO.instructions() != null && functionDTO.instructions().programInstructionsDTOList() != null) {
                numInstructions = functionDTO.instructions().programInstructionsDTOList().size();
            }

            int maxDegree = 0;
            try { maxDegree = engine.getMaxDegree(functionName); } catch (Exception ignored) {}

            String mainProgramName = function.getMainProgramName() == null ? "" : function.getMainProgramName();
            String uploadedBy = operation.getUserUploaded();

            outFunctionsList.add(new AvailableFunctionDTO(
                    functionName,
                    functionUserString,
                    mainProgramName,
                    uploadedBy,
                    numInstructions,
                    maxDegree
            ));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        GSON_INSTANCE.toJson(outFunctionsList, response.getWriter());
    }
}
