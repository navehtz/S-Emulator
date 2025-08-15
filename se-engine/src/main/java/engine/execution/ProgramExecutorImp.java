package engine.execution;

import engine.instruction.Instruction;
import engine.label.FixedLabel;
import engine.label.Label;
import engine.program.Program;
import engine.variable.Variable;
import engine.variable.VariableImp;
import engine.variable.VariableType;

import java.util.Map;

public class ProgramExecutorImp implements ProgramExecutor{

    private final Program program;


    public ProgramExecutorImp(Program program) {
        this.program = program;
    }

    @Override
    public long run(Long... inputs) {
        ExecutionContext context = null;
        Instruction currentInstruction = program.getInstructionsList().get(0);
        Label nextLabel;

        InitializeVariables(context, inputs);

        do {
            nextLabel = currentInstruction.execute(context);

            if(nextLabel == FixedLabel.EMPTY) {
                int indexOfNextInstruction = program.getInstructionsList().indexOf(currentInstruction) + 1;
                currentInstruction = program.getInstructionsList().get(indexOfNextInstruction);
            }
            else if(nextLabel != FixedLabel.EXIT) {
                currentInstruction = program.labelToInstruction().get(nextLabel);
            }

        } while(nextLabel != FixedLabel.EXIT);

        return context.getVariableValue(Variable.RESULT);
    }

    private void InitializeVariables(ExecutionContext context, Long... inputs) {
        for (int i = 0; i < inputs.length; i++) {
            Variable newVariable = new VariableImp(VariableType.INPUT, (i + 1));
            context.updateVariable(newVariable, inputs[i]);
        }

        context.updateVariable(Variable.RESULT, 0L);
    }

    @Override
    public Map<Variable, Long> variableToValue() {
        return Map.of();
    }
}
