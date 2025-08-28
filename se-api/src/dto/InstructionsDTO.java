package dto;

import java.util.ArrayList;
import java.util.List;

public class InstructionsDTO {
    private List<String> programInstructionsStr = new ArrayList<>();

    public InstructionsDTO(List<String> programInstructionsStr) {
        this.programInstructionsStr = programInstructionsStr;
    }

    public List<String> getProgramInstructionsStr() {
        return programInstructionsStr;
    }
}
