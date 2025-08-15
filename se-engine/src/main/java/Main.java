import engine.instruction.DecreaseInstruction;
import engine.instruction.IncreaseInstruction;
import engine.instruction.Instruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.label.LabelImp;
import engine.program.Program;
import engine.program.ProgramImp;
import engine.variable.Variable;
import engine.variable.VariableImp;
import engine.variable.VariableType;

public class Main {
    public static void main(String[] args) {

        System.out.println("OK");

        Variable x1 = new VariableImp(VariableType.INPUT, 1);
        Variable z1 = new VariableImp(VariableType.WORK, 1);

        Label l1 = new LabelImp(100);
        Label l2 = FixedLabel.EXIT;

        Instruction increase = new IncreaseInstruction(x1, l1);
        Instruction decrease = new DecreaseInstruction(z1, l2);

        Program p = new ProgramImp("test");
        p.addInstruction(increase);
        p.addInstruction(decrease);
    }
}
