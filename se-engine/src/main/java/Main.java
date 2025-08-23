import dto.ProgramApi;
import engine.exceptions.EngineLoadException;
import engine.execution.ExecutionHistory;
import engine.execution.ExecutionHistoryImpl;
import engine.execution.ProgramExecutor;
import engine.execution.ProgramExecutorImpl;
import engine.instruction.*;
import engine.program.Program;
import engine.variable.Variable;
import engine.xmlStructure.loader.XmlProgramLoader;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 .\xjc-run-win.bat -p engine.generated -d "..\..\se-engine\src\main\java" S-Emulator-v1.xsd
 */

public class Main {
    public static void main(String[] args) throws EngineLoadException, URISyntaxException {
        XmlProgramLoader loader = new XmlProgramLoader();
        ExecutionHistory executionHistory = new ExecutionHistoryImpl();
        ProgramExecutor pe = null;
        Program p = null;
        Path xmlPath = null;
        ProgramApi programApi;


        try {
            xmlPath = Paths.get("se-engine", "src", "main", "resources", "xml-samples", "test.xml");
            p = loader.load(xmlPath);
            p.validateProgram();
            p.initialize();

            pe = new ProgramExecutorImpl(p);

            // להציג למשתמש את דרגת ההרצה המקסימלית
            int maxDegree = pe.calculateProgramMaxDegree();
            System.out.println("Max degree: " + maxDegree);

            //לבחור דרגת הרצה מ0 ועד מקסימום
            int runDegree = 0;

            // להציג למשתמש את משתני הקלט שהתוכנית משתמשת בהם
            String inputDisplay = pe.getInputVariablesOfProgram().stream()
                    .map(Variable::getRepresentation)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            System.out.println("Input used in program: " + inputDisplay);

            // להרחיב את התוכנית בהתאם לקלט מהמשתמש (דרגת ההרצה)
            pe.extendProgram(runDegree);

            // להריץ את התוכנית (וגם לשמור היסטוריה, לאתחל משתנים..)
            long res = pe.run(5L, 10L, 15L);

            // להציג למשתמש את התוכנית
            programApi = pe.getProgramApi();
            System.out.println(programApi.getProgramDisplay());

            // להציג למשתמש את Y
            System.out.println("y = " + res);

            // להציג למשתמש את משתני התוכנית וערכם

            // להציג למשתמש את כמות הCYCLES שנצרכה

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
