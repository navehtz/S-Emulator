package ui.behavior;

import dto.execution.InstructionDTO;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.function.Function;

public class HighlightingBehavior {
    private static final String HIGHLIGHT_CLASS = "var-highlight";

    public void wire(TableView<InstructionDTO> table,
                     ComboBox<String> selectorComboBox,
                     Function<InstructionDTO, String> commandExtractor) {

        Runnable refresh = table::refresh;
        selectorComboBox.valueProperty().addListener((o,ov,nv) -> refresh.run());
        table.itemsProperty().addListener((o,ov,nv) -> refresh.run());

        table.setRowFactory(tv -> {
            TableRow<InstructionDTO> row = new TableRow<>();

            ChangeListener<Object> upd = (o, ov, nv) -> apply(row, selectorComboBox, commandExtractor);
            row.itemProperty().addListener(upd);
            row.indexProperty().addListener(upd);
            tv.getSelectionModel().selectedIndexProperty().addListener(upd);
            selectorComboBox.valueProperty().addListener(upd);

            return row;
        });
    }

    private void apply(TableRow<InstructionDTO> row,
                       ComboBox<String> selectorComboBox,
                       Function<InstructionDTO, String> commandExtractor) {

        row.getStyleClass().remove(HIGHLIGHT_CLASS);

        InstructionDTO item = row.getItem();
        if (item == null) return;

        String picked = selectorComboBox.getValue();
        if (picked == null || picked.isBlank()) return;

        String cmd = commandExtractor.apply(item);
        boolean match = cmd != null && cmd.matches(".*\\b" + java.util.regex.Pattern.quote(picked) + "\\b.*");

        if (match && !row.isSelected()) {
            row.getStyleClass().add(HIGHLIGHT_CLASS);
        }
    }
}
