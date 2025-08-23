import dto.ProgramApi;
import engine.exceptions.EngineLoadException;
import engine.execution.ExecutionHistory;
import engine.execution.ExecutionHistoryImpl;
import engine.execution.ProgramExecutor;
import engine.execution.ProgramExecutorImpl;
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
        ProgramExecutor pe2 = null;
        Program p = null;
        Path xmlPath = null;
        ProgramApi programApi;


        try {
            xmlPath = Paths.get("se-engine", "src", "main", "resources", "xml-samples", "test12.xml");
            p = loader.load(xmlPath);
            p.validateProgram();
            p.initialize();

            pe = new ProgramExecutorImpl(p);
            pe2 = new ProgramExecutorImpl(p);

            // להציג למשתמש את דרגת ההרצה המקסימלית
            int maxDegree = pe.calculateProgramMaxDegree();
            System.out.println("Max degree: " + maxDegree);

            //לבחור דרגת הרצה מ0 ועד מקסימום
            int runDegree = 0;

            // להציג למשתמש את משתני הקלט שהתוכנית משתמשת בהם
            String inputDisplay = pe.getInputVariablesSet().stream()
                    .map(Variable::getRepresentation)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            System.out.println("Input used in program: " + inputDisplay);

            // להרחיב את התוכנית בהתאם לקלט מהמשתמש (דרגת ההרצה)
            pe.extendProgram(runDegree);

/*            System.out.println();
            System.out.println("Extend Instruction: ");
            System.out.println(pe.getExtendedProgramDisplay());
            System.out.println();*/

            // להריץ את התוכנית (וגם לשמור היסטוריה, לאתחל משתנים..)
            long res = pe.run(5L, 10L, 15L);
            executionHistory.addProgramToHistory(pe);

            // להציג למשתמש את התוכנית
            //System.out.println(pe.getProgramDisplay());

            // להציג למשתמש את Y
/*            System.out.println("y = " + res);
            System.out.println();*/

            // להציג למשתמש את משתני התוכנית וערכם
/*            String inputAndWork = pe.getInputAndWorkVariablesWithValuesDisplay();
            System.out.println("InputAndWork: ");
            System.out.println(inputAndWork);
            System.out.println();*/

            // להציג למשתמש את כמות הCYCLES שנצרכה
/*            int cycles = pe.getTotalCyclesOfProgram();
            System.out.println("Total cycles: " + cycles);*/



        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
