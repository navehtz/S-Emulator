package ui.components;

import dto.InstructionDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class InstructionTableController {

    /*public static class InstructionRow {
        public final int idx;           // 1-based
        public final String type;       // "B"/"S"
        public final String label;      // null or "EXIT" or "L2"
        public final String command;    // display text
        public final int cycles;

        public InstructionRow(int idx, String type, String label, String command, int cycles) {
            this.idx = idx; this.type = type; this.label = label; this.command = command; this.cycles = cycles;
        }
    }*/

    @FXML private TableView<InstructionDTO> table;
    @FXML private TableColumn<InstructionDTO, Number> colIdx;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colCommand;
    @FXML private TableColumn<InstructionDTO, Number> colCycles;

    private final ObservableList<InstructionDTO> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colIdx.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().instructionNumber()));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().instructionTypeStr()));
        colLabel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().labelStr()));
        colCommand.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().command()));
        colCycles.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().cycleNumber()));
        table.setItems(rows);
    }

    /*private String box(String label) {
        if (label == null || label.isBlank()) return "[     ]";
        String s = " " + label + "  ";
        String inside = s.length() > 5 ? s.substring(0,5) : String.format("%-5s", s);
        return "[" + inside + "]";
    }*/

    public void setItems(Iterable<InstructionDTO> items) {
        rows.clear();
        for (InstructionDTO r : items) rows.add(r);
    }

    public TableView<InstructionDTO> getTable() { return table; }
    public InstructionDTO getSelected() { return table.getSelectionModel().getSelectedItem(); }
}
