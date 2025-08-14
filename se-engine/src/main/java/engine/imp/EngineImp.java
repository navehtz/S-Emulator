package engine.imp;

import engine.*;
import engine.domain.Instruction;
import engine.domain.Program;
import engine.exceptions.EngineLoadException;
import engine.exceptions.EngineRunException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EngineImp implements Engine {

    private Program current;

    public EngineImp() {
        List<RunSummary> history = new ArrayList<>();
    }

    @Override
    public boolean hasProgram() {
        return current != null;
    }

    @Override
    public void loadFromXml(Path xmlPath) throws EngineLoadException {

    }

    @Override
    public String getProgramDisplay() {
        ensureProgramLoaded();

        List<String> inputs = current.inputsOrdered();
        List<String> labels = reorderLabelsExitLast(current.labelsOrdered());

        StringBuilder programDisplay = new StringBuilder();
        programDisplay.append("Program: ").append(current.name()).append(System.lineSeparator());
        programDisplay.append("Inputs: ").append(String.join(", ", inputs)).append(System.lineSeparator());
        programDisplay.append("Labels: ").append(String.join(", ", labels)).append(System.lineSeparator());
        programDisplay.append("Instructions: ").append(System.lineSeparator());

        for(Instruction instruction : current.instructions()) {
            programDisplay.append(instruction.toString()).append(System.lineSeparator());
        }

        return programDisplay.toString();
    }

    private static List<String> reorderLabelsExitLast(List<String> labelsInOrder) {
        if(labelsInOrder == null) {
           throw new IllegalArgumentException("labelsInOrder is null");
        }

        if(labelsInOrder.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> out =  new ArrayList<>(labelsInOrder.size());
        boolean hasExit = false;

        for(String label : labelsInOrder) {
            if(label == null) {
                throw new IllegalArgumentException ("Labels cannot be null");
            }

            String trimmedLabel = label.trim();

            if(trimmedLabel.equalsIgnoreCase("Exit")) {
                hasExit = true;
            }
            else {
                out.add(trimmedLabel);
            }
        }

        if(hasExit) {
            out.add("EXIT");
        }

        return out;
    }

    @Override
    public RunResult run() throws EngineRunException {
        return null;
    }

    @Override
    public List<RunSummary> getHistory() {
        return List.of();
    }

    private void ensureProgramLoaded() {
        if (current == null) {
            throw new IllegalStateException("No program loaded");
        }
    }
}
