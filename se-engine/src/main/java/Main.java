import dto.ProgramApi;
import engine.execution.ProgramExecutor;
import engine.execution.ProgramExecutorImpl;
import engine.instruction.*;
import engine.instruction.basic.DecreaseInstruction;
import engine.instruction.basic.IncreaseInstruction;
import engine.instruction.basic.JumpNotZeroInstruction;
import engine.instruction.basic.NoOpInstruction;
import engine.instruction.synthetic.ConstantAssignmentInstruction;
import engine.instruction.synthetic.JumpEqualVariableInstruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.label.LabelImpl;
import engine.program.Program;
import engine.program.ProgramImpl;
import engine.variable.Variable;
import engine.variable.VariableImpl;
import engine.variable.VariableType;

public class Main {
    public static void main(String[] args) {
        check1();
    }




    private static void check2() {

        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable x2 = new VariableImpl(VariableType.INPUT, 2);
        Label l1 = FixedLabel.EXIT;

        Program p = new ProgramImpl("CHECK");
        //p.addInstruction(new ZeroVariableInstruction(x1));
        //p.addInstruction(new AssignmentInstruction(x1, x2));
        //p.addInstruction(new AssignmentInstruction(Variable.RESULT, x1));

        p.addInstruction(new ConstantAssignmentInstruction(Variable.RESULT, 1));

        p.addInstruction(new JumpEqualVariableInstruction(x1, x2, l1));
        p.addInstruction(new ConstantAssignmentInstruction(Variable.RESULT, 5));

        //p.addInstruction(new JumpEqualConstantInstruction(x1, 5, l1));

        ProgramExecutor pe = new ProgramExecutorImpl(p);
        ProgramApi programApi = pe.getProgramApi();
        String display = programApi.programDisplay();
        System.out.println(display);

        long result = new ProgramExecutorImpl(p).run(4L, 8L, 12L);
        System.out.println(result);
    }

    private static void check1() {
        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable z1 = new VariableImpl(VariableType.WORK, 1);

        Label l1 = new LabelImpl(10);
        Label l3 = new LabelImpl(20);
        Label l2 = FixedLabel.EXIT;

        Instruction increase = new IncreaseInstruction(x1, l1);
        Instruction decrease = new DecreaseInstruction(z1, l2);
        Instruction JMZ = new JumpNotZeroInstruction(x1, l1);
        Instruction NoOP = new NoOpInstruction(z1, l3);

        Program p = new ProgramImpl("test");
        p.addInstruction(increase);
        p.addInstruction(decrease);
        p.addInstruction(JMZ);
        p.addInstruction(NoOP);

        ProgramExecutor pe = new ProgramExecutorImpl(p);

        ProgramApi programApi = pe.getProgramApi();

        String display = programApi.programDisplay();
        System.out.println(display);
    }

    private static void sanity() {
        /*

        {y = x1}

        [L1] x1 ← x1 – 1
             y ← y + 1
             IF x1 != 0 GOTO L1
        * */

        Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        LabelImpl l1 = new LabelImpl(1);

        Program p = new ProgramImpl("SANITY");
        p.addInstruction(new DecreaseInstruction(x1, l1));
        p.addInstruction(new IncreaseInstruction(Variable.RESULT));
        p.addInstruction(new JumpNotZeroInstruction(x1, l1));

        ProgramExecutor pe = new ProgramExecutorImpl(p);
        ProgramApi programApi = pe.getProgramApi();
        String display = programApi.programDisplay();
        System.out.println(display);

        long result = new ProgramExecutorImpl(p).run(4L);
        System.out.println(result);
    }
}
