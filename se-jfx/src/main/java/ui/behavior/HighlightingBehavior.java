package ui.behavior;

import dto.InstructionDTO;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.function.Function;


public class HighlightingBehavior {
    private final PseudoClass highlightedPseudoClass;

    public HighlightingBehavior(String pseudoClassName) {
        this.highlightedPseudoClass = PseudoClass.getPseudoClass(pseudoClassName);
    }

    public void wire(TableView<InstructionDTO> table,
                     ComboBox<String> selectorComboBox,
                     Function<InstructionDTO, String> commandExtractor) {

        selectorComboBox.valueProperty().addListener((obs, oldVal, newVal) -> table.refresh());

        table.itemsProperty().addListener((obs, oldVal, newVal) -> table.refresh());

        table.setRowFactory(_tv -> {
            TableRow<InstructionDTO> row = new TableRow<>();
            ChangeListener<Object> applier = (obs, oldVal, newValue) -> apply(row, selectorComboBox, commandExtractor);
            row.itemProperty().addListener(applier);
            row.selectedProperty().addListener(applier);
            return row;
        });
    }

    private void apply(TableRow<InstructionDTO> row,
                       ComboBox<String> selectorComboBox,
                       Function<InstructionDTO, String> commandExtractor) {
        InstructionDTO instructionFromRow = row.getItem();
        String chosenVariable = selectorComboBox.getValue();

        boolean isRowChosen = chosenVariable != null && !chosenVariable.isBlank();
        boolean isMatchedRows = isRowChosen && instructionFromRow != null
                && containsVariable(commandExtractor.apply(instructionFromRow), chosenVariable);

        boolean isApply = isMatchedRows && !row.isSelected();
        row.pseudoClassStateChanged(highlightedPseudoClass, isApply);
    }

    private boolean containsVariable(String command, String variable) {
        //return command != null && command.contains(variable);
        if (command == null || variable == null || variable.isEmpty()) return false;

        return command.matches(".*\\b" + java.util.regex.Pattern.quote(variable) + "\\b.*");
    }
}
