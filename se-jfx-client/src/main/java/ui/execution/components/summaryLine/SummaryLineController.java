package ui.execution.components.summaryLine;

import architecture.ArchitectureType;
import dto.execution.InstructionDTO;
import dto.execution.ProgramDTO;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.ObjectProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;


public class SummaryLineController {

    public Label totalInstructions;
    public Label iArchInstructions;
    public Label amountIArch;
    public Label iiArchInstructions;
    public Label amountIIArch;
    public Label iiiArchInstructions;
    public Label amountIIIArch;
    public Label ivArchInstructions;
    public Label amountIVArch;
    private ObjectProperty<ProgramDTO> currentSelectedProgramProperty;

    private static final PseudoClass OVER_CAP = PseudoClass.getPseudoClass("overcap");

    @FXML private Label amountTotal;
//    @FXML private Label amountBasic;
//    @FXML private Label amountSynthetic;


    public void setProperty(ObjectProperty<ProgramDTO> programProperty) {
        this.currentSelectedProgramProperty = programProperty;
    }

    public void initializeBindings() {
        if (currentSelectedProgramProperty == null) {
            return;
        }

        // Integer bindings that recompute counts whenever currentProgramProperty changes
        IntegerBinding totalCountBinding = Bindings.createIntegerBinding(
                () -> computeTotalInstructionsCount(currentSelectedProgramProperty.get()), currentSelectedProgramProperty);

        IntegerBinding basicCountBinding = Bindings.createIntegerBinding(
                () -> computeBasicInstructionsCount(currentSelectedProgramProperty.get()), currentSelectedProgramProperty);

        IntegerBinding syntheticCountBinding = Bindings.createIntegerBinding(
                () -> totalCountBinding.get() - basicCountBinding.get(), totalCountBinding, basicCountBinding);

        IntegerBinding archICountBinding = Bindings.createIntegerBinding(
                () -> computeArchICount(currentSelectedProgramProperty.get()), currentSelectedProgramProperty);

        IntegerBinding archIICountBinding = Bindings.createIntegerBinding(
                () -> computeArchIICount(currentSelectedProgramProperty.get()), currentSelectedProgramProperty);

        IntegerBinding archIIICountBinding = Bindings.createIntegerBinding(
                () -> computeArchIIICount(currentSelectedProgramProperty.get()), currentSelectedProgramProperty);

        IntegerBinding archIVCountBinding = Bindings.createIntegerBinding(
                () -> computeArchIVCount(currentSelectedProgramProperty.get()), currentSelectedProgramProperty);

        // Bind labels to the string bindings
        amountTotal.textProperty().bind(totalCountBinding.asString());
        amountIArch.textProperty().bind(archICountBinding.asString());
        amountIIArch.textProperty().bind(archIICountBinding.asString());
        amountIIIArch.textProperty().bind(archIIICountBinding.asString());
        amountIVArch.textProperty().bind(archIVCountBinding.asString());
    }

    private int computeTotalInstructionsCount(ProgramDTO program) {
        if (program == null || program.instructions() == null) {
            return 0;
        }

        List<InstructionDTO> list = program.instructions().programInstructionsDTOList();
        return list == null ? 0 : list.size();
    }

    private int computeBasicInstructionsCount(ProgramDTO program) {
        if (program == null || program.instructions() == null) {
            return 0;
        }

        List<InstructionDTO> list = program.instructions().programInstructionsDTOList();
        if (list == null) return 0;

        // Filter all instructions where instructionTypeStr equals "B"
        return (int) list.stream()
                .filter(instr -> "B".equals(instr.instructionTypeStr()))
                .count();
    }

    private int computeArchICount(ProgramDTO program) {
        if (program == null || program.instructions() == null) {
            return 0;
        }

        List<InstructionDTO> list = program.instructions().programInstructionsDTOList();
        if (list == null) return 0;

        return (int) list.stream()
                .filter(instr -> "I".equals(instr.architectureStr()))
                .count();
    }

    private int computeArchIICount(ProgramDTO program) {
        if (program == null || program.instructions() == null) {
            return 0;
        }

        List<InstructionDTO> list = program.instructions().programInstructionsDTOList();
        if (list == null) return 0;

        return (int) list.stream()
                .filter(instr -> "II".equals(instr.architectureStr()))
                .count();
    }

    private int computeArchIIICount(ProgramDTO program) {
        if (program == null || program.instructions() == null) {
            return 0;
        }

        List<InstructionDTO> list = program.instructions().programInstructionsDTOList();
        if (list == null) return 0;

        return (int) list.stream()
                .filter(instr -> "III".equals(instr.architectureStr()))
                .count();
    }

    private int computeArchIVCount(ProgramDTO program) {
        if (program == null || program.instructions() == null) {
            return 0;
        }

        List<InstructionDTO> list = program.instructions().programInstructionsDTOList();
        if (list == null) return 0;

        return (int) list.stream()
                .filter(instr -> "IV".equals(instr.architectureStr()))
                .count();
    }

    public void setArchitectureCap(ArchitectureType architectureType) {
        int rank = architectureType.getRank();
        tint(iArchInstructions,   1 > rank);
        tint(iiArchInstructions,  2 > rank);
        tint(iiiArchInstructions, 3 > rank);
        tint(ivArchInstructions,  4 > rank);
    }

    private void tint(Label label, boolean on) {
        if (label != null) label.pseudoClassStateChanged(OVER_CAP, on);
    }
}
