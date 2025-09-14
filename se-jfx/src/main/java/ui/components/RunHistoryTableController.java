package ui.components;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class RunHistoryTableController {

    public static class RunRow {
        public final int runNum;
        public final String inputs; // e.g., "x1=3,x3=1"
        public final int y;
        public final int cycles;
        public RunRow(int runNum, String inputs, int y, int cycles) {
            this.runNum = runNum; this.inputs = inputs; this.y = y; this.cycles = cycles;
        }
    }

    @FXML private TableView<RunRow> table;
    @FXML private TableColumn<RunRow, Number> colRunNum;
    @FXML private TableColumn<RunRow, String> colInputs;
    @FXML private TableColumn<RunRow, Number> colY;
    @FXML private TableColumn<RunRow, Number> colCycles;

    @FXML public Button btnShow;
    @FXML public Button btnRerun;

    private final ObservableList<RunRow> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colRunNum.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().runNum));
        colInputs.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().inputs));
        colY.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().y));
        colCycles.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().cycles));
        table.setItems(rows);
    }

    public void setRows(List<RunRow> list) {
        rows.setAll(list);
    }

    public RunRow getSelected() { return table.getSelectionModel().getSelectedItem(); }
}
