package debug;

import dto.DebugDTO;
import dto.ProgramExecutorDTO;
import engine.ProgramRegistry;
import execution.*;
import instruction.Instruction;
import label.FixedLabel;
import label.Label;
import operation.OperationView;
import variable.Variable;

import java.util.*;

public class DebugImpl implements Debug {
    private final OperationView program;
    private final ProgramRegistry registry;
    private final ExecutionContext context;
    private final List<Long> inputsValuesOfUser;
    private final int degree;

    private final List<Instruction> instructions;
    private int currentInstructionIndex;
    private int nextInstructionIndex;
    private int currentCycles;

    private String targetVariableRepresentation;

    private final List<DebugDTO> stepsHistory = new ArrayList<>();
    private int historyIndex = -1;
    private boolean justStoppedOnBreakPoint = false;

    public DebugImpl(OperationView program,
                     ProgramRegistry registry,
                     int degree,
                     List<Long> inputs) {
        this.program = Objects.requireNonNull(program, "program");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.degree = degree;
        this.inputsValuesOfUser = Objects.requireNonNullElseGet(inputs, List::of);

        // Build an execution context wired to registry and invoker
        this.context = new ExecutionContextImpl(
                registry,
                new ProgramExecutorInvoker(registry) // implements OperationInvoker
        );

        // initialize variables for the selected program/function
        this.context.initializeVariables(program, this.inputsValuesOfUser.toArray(Long[]::new));

        this.instructions = program.getInstructionsList();
    }

    @Override
    public DebugDTO init() {
        return setAndReturnInitSnapshot();
    }

    private DebugDTO setAndReturnInitSnapshot() {
        currentInstructionIndex = 0;
        nextInstructionIndex = 0;
        currentCycles = 0;
        context.initializeVariables(program, inputsValuesOfUser.toArray(Long[]::new));
        DebugDTO initSnap = buildSnapshotDTO();
        stepsHistory.clear();
        stepsHistory.add(initSnap);
        historyIndex = 0;
        return initSnap;
    }

    @Override
    public DebugDTO resume(List<Boolean> breakPoints) throws InterruptedException {
        if (historyIndex < 0 && hasMoreInstructions()) {
            if (stopHereOnBreakpoint(breakPoints)) {
                return stepsHistory.get(historyIndex);
            }
            stepOnceAndSave();
        }

        if (justStoppedOnBreakPoint) {
            justStoppedOnBreakPoint = false;
            stepOnceAndSave();
        }

        while (hasMoreInstructions()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Debug cancelled by user");
            }
            if (stopHereOnBreakpoint(breakPoints)) {
                return stepsHistory.get(historyIndex);
            }
            stepOnceAndSave();
        }

        DebugDTO finalSnap = buildSnapshotDTO();
        stepsHistory.add(finalSnap);
        historyIndex = stepsHistory.size() - 1;
        return finalSnap;
    }

    @Override
    public DebugDTO stepOver() {
        if(!hasMoreInstructions()) {
            return (historyIndex >= 0) ? stepsHistory.get(historyIndex) : buildSnapshotDTO();
        }
        if (historyIndex == stepsHistory.size() - 1) {
            stepOnceAndSave();
        } else {
            historyIndex++;
        }
        return stepsHistory.get(historyIndex);
    }

    @Override
    public DebugDTO stepBack() {
        historyIndex--;
        if (historyIndex < 0) {
            // reset
            return setAndReturnInitSnapshot();
        }

        DebugDTO snap = stepsHistory.get(historyIndex);
        this.currentInstructionIndex = snap.currentInstructionNumber();
        this.nextInstructionIndex = snap.nextInstructionNumber();
        this.currentCycles = snap.totalCycles(); //TODO: check if it's good
        return snap;
    }

    @Override
    public DebugDTO stop() {
        DebugDTO snap;
        if (historyIndex >= 0) {
            snap = stepsHistory.get(historyIndex);
            //stepsHistory.clear();
            historyIndex = -1;
        } else {
            snap = buildSnapshotDTO();
        }
        return snap;
    }

    @Override
    public boolean hasMoreInstructions() {
        if (currentInstructionIndex >= instructions.size()) return false;
        return !instructions.get(currentInstructionIndex).getLabel().equals(FixedLabel.EXIT);
    }

//    @Override
//    public ProgramExecutorDTO buildProgramExecutorDTO(ProgramExecutor programExecutor) {
//        return null;
//    }

    @Override
    public int getCurrentInstructionIndex() {
        return currentInstructionIndex;
    }

    @Override
    public int getNextInstructionIndex() {
        return nextInstructionIndex;
    }


    // helpers

    private void stepOnceAndSave() {
        stepOnceNoHistory();
        DebugDTO snap = buildSnapshotDTO();
        stepsHistory.add(snap);
        historyIndex = stepsHistory.size() - 1;
    }

    private void stepOnceNoHistory() {
        if (currentInstructionIndex >= instructions.size()) return;

        Instruction currentInstruction = instructions.get(currentInstructionIndex);

        Label next = currentInstruction.execute(context);
        currentCycles += currentInstruction.getCycleOfInstruction();

        targetVariableRepresentation = (currentInstruction.getTargetVariable() != null)
                ? currentInstruction.getTargetVariable().getRepresentation()
                : null;

        updateNextIndex(next);
        currentInstructionIndex = nextInstructionIndex;
    }

    private void updateNextIndex(Label nextLabel) {
        if (nextLabel.equals(FixedLabel.EMPTY)) {
            nextInstructionIndex++;
        } else if (nextLabel.equals(FixedLabel.EXIT)) {
            nextInstructionIndex = instructions.size();
        } else {
            Instruction dest = program.getLabelToInstruction().get(nextLabel);
            nextInstructionIndex = (dest != null) ? (dest.getInstructionNumber() - 1) : instructions.size();
        }
    }
    private boolean stopHereOnBreakpoint(List<Boolean> breakpoints) {
        if (breakpoints == null) return false;
        if (currentInstructionIndex >= breakpoints.size()) return false;
        if (!breakpoints.get(currentInstructionIndex)) return false;

        DebugDTO snap = buildSnapshotDTO();
        stepsHistory.add(snap);
        historyIndex = stepsHistory.size() - 1;
        justStoppedOnBreakPoint = true;
        return true;
    }

    private DebugDTO buildSnapshotDTO() {
        Map<String, Long> varsToValues = new LinkedHashMap<>();
        for (Variable v : program.getInputAndWorkVariablesSortedBySerial()) {
            varsToValues.put(v.getRepresentation(), context.getVariableValue(v));
        }
        long result = context.getOperationResult();

        return new DebugDTO(
                program.getName(),
                currentInstructionIndex,
                nextInstructionIndex,
                hasMoreInstructions(),
                targetVariableRepresentation,
                degree,
                result,
                currentCycles,
                varsToValues
        );
    }
}
