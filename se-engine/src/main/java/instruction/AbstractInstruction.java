package instruction;

import label.Label;
import label.FixedLabel;
import program.Program;
import variable.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;


public abstract class AbstractInstruction implements Instruction {

    private final InstructionData instructionData;
    private final InstructionType instructionType;
    private final int instructionNumber;
    private final Label label;
    private final Variable targetVariable;
    private final Instruction origin;
    private Program programOfThisInstruction = null;

    public AbstractInstruction(InstructionData instructionData, InstructionType instructionType, Variable targetVariable, Instruction origin, int instructionNumber) {
        this(instructionData, instructionType, targetVariable,FixedLabel.EMPTY, origin, instructionNumber);
    }

    public AbstractInstruction(InstructionData instructionData, InstructionType instructionType, Variable targetVariable, Label label, Instruction origin, int instructionNumber) {
        this.instructionData = instructionData;
        this.instructionType = instructionType;
        this.targetVariable = targetVariable;
        this.label = label;
        this.origin = origin;
        this.instructionNumber = instructionNumber;
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
    public int getInstructionNumber() {
        return this.instructionNumber;
    }

    @Override
    public String getInstructionRepresentation(int numberOfInstructionsInProgram) {
        int labelPadding = 3;
        int numberPadding = numberOfInstructionsInProgram == 0 ? 1
                : (int) LongStream.iterate(Math.abs(numberOfInstructionsInProgram), x -> x > 0, x -> x / 10).count();

        String label = getLabel() == null ? "" : getLabel().getLabelRepresentation();

        return String.format(
                "#%" + numberPadding + "d (%s)[ %-" + labelPadding + "s ] %-" + 5 + "s (%d)",
                this.instructionNumber,
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

/*    @Override
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
    }*/

    @Override
    public List<Instruction> getExtendedInstruction() {

        if (this instanceof SyntheticInstruction syntheticInstruction) {
            return syntheticInstruction.getInnerInstructions();
        }

        return List.of(this);   // Basic instruction -> keep as it is
    }

    public Program getProgramOfThisInstruction() {
        return programOfThisInstruction;
    }

    @Override
    public void setProgramOfThisInstruction(Program programOfThisInstruction) {
        this.programOfThisInstruction = programOfThisInstruction;
    }

    @Override
    public Instruction getOriginalInstruction() {
        return origin;
    }

    @Override
    public List<String> getInstructionExtendedDisplay(int numberOfInstructionsInProgram) {
        if (this instanceof OriginOfAllInstruction) {
            return Collections.emptyList();
        }

        List<String> ancestors = origin.getInstructionExtendedDisplay(numberOfInstructionsInProgram);
        String current = getInstructionRepresentation(numberOfInstructionsInProgram);

        if (ancestors == null || ancestors.isEmpty()) {
            return List.of(current);
        }

        List<String> chain = new ArrayList<>(1 + ancestors.size());
        chain.add(current);
        chain.addAll(ancestors);
        return chain;
    }

/*    @Override
    public String getInstructionExtendedDisplay(int numberOfInstructionsInProgram) {
        if(this instanceof OriginOfAllInstruction) {
            return "";
        }

        String ancestorsDisplay = origin.getInstructionExtendedDisplay(numberOfInstructionsInProgram);
        String currentDisplay = getInstructionRepresentation(numberOfInstructionsInProgram);

        return ancestorsDisplay.isEmpty()
                ? currentDisplay
                : currentDisplay + "  <<<  " + ancestorsDisplay;
    }*/
}