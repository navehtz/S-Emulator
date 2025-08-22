package engine.instruction;

import engine.label.Label;
import engine.label.FixedLabel;
import engine.program.Program;
import engine.variable.Variable;

import java.util.List;
import java.util.stream.LongStream;


public abstract class AbstractInstruction implements Instruction {

    private final InstructionData instructionData;
    private final InstructionType instructionType;
    private final Label label;
    private final Variable targetVariable;
    private Program programOfThisInstruction = null;

    public AbstractInstruction(InstructionData instructionData, InstructionType instructionType, Variable targetVariable) {
        this(instructionData, instructionType, targetVariable,FixedLabel.EMPTY);
    }

    public AbstractInstruction(InstructionData instructionData, InstructionType instructionType, Variable targetVariable, Label label) {
        this.instructionData = instructionData;
        this.instructionType = instructionType;
        this.targetVariable = targetVariable;
        this.label = label;
    }

    @Override
    public String getName() {
        return this.instructionData.getName();
    }

    @Override
    public Label getLabel() {
        return this.label;
    }

    @Override
    public Variable getTargetVariable() {
        return this.targetVariable;
    }

    @Override
    public Variable getSourceVariable() {
        return null;
    }

    @Override
    public String instructionRepresentation(int numberOfInstructionsInProgram, int InstructionNumber) {
        int labelPadding = 3;
        int numberPadding = numberOfInstructionsInProgram == 0 ? 1
                : (int) LongStream.iterate(Math.abs(numberOfInstructionsInProgram), x -> x > 0, x -> x / 10).count();

        String label = getLabel() == null ? "" : getLabel().getLabelRepresentation();

        return String.format(
                "#%" + numberPadding + "d (%s)[ %-" + labelPadding + "s ] %-" + 5 + "s (%d)",
                InstructionNumber,
                instructionType.getInstructionType(),
                label,
                getCommand(),
                getCycleOfInstruction()
        );
    }

    @Override
    public int getCycleOfInstruction() {
        return instructionData.getCycles();
    }

    @Override
    public int calculateInstructionMaxDegree(Program program) {

        if (this instanceof SyntheticInstruction syntheticInstruction) {

            this.setProgramOfThisInstruction(program);
            syntheticInstruction.setInnerInstructions();

            int innerMax = 0;
            for(Instruction innerInstruction : syntheticInstruction.getInnerInstructions()) {
                  innerMax = Math.max(innerMax, innerInstruction.calculateInstructionMaxDegree(program));
            }

            return innerMax + 1;
        }

        return 0;
    }

    @Override
    public List<Instruction> getExtendedInstruction() {

        if (this instanceof SyntheticInstruction syntheticInstruction) {
            return syntheticInstruction.getInnerInstructions();
        }

        return List.of(this);   // Basic instruction -> keep as it is
    }

/*    @Override
    public List<Instruction> getExtendedInstruction(int degree, ExecutionContext context, Program program) {
        if (degree <= 0) {
            return List.of(this);
        }

        if (this instanceof SyntheticInstruction syntheticInstruction) {
            List<Instruction> extendedInstructions = new ArrayList<>();

            for(Instruction innerInstruction : syntheticInstruction.getInnerInstructions()) {
                extendedInstructions.addAll(innerInstruction.getExtendedInstruction(degree - 1, context, program));

            }
            return extendedInstructions;
        }

        return List.of(this);   // Basic instruction -> keep as it is
    }*/

    public Program getProgramOfThisInstruction() {
        return programOfThisInstruction;
    }

    @Override
    public void setProgramOfThisInstruction(Program programOfThisInstruction) {
        this.programOfThisInstruction = programOfThisInstruction;
    }
}