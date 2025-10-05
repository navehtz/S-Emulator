package ui.components;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;

public class RunHistoryTableController {


    public record RunRow(int runNum, int degree, /*String inputs*/ long result, int cycles) {
    }

    @FXML private TableView<RunRow> table;
    @FXML private TableColumn<RunRow, Number> colRunNum;
    @FXML private TableColumn<RunRow, Number> colDegree;
    //@FXML private TableColumn<RunRow, String> colInputs;
    @FXML private TableColumn<RunRow, Number> colResult;
    @FXML private TableColumn<RunRow, Number> colCycles;

    @FXML public Button btnShow;
    @FXML public Button btnRerun;

    private final ObservableList<RunRow> rows = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colRunNum.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().runNum));
        colDegree.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().degree));
        //colInputs.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().inputs));
        colResult.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().result));
        colCycles.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().cycles));
        table.setItems(rows);
    }

    public void setRows(List<RunRow> list) {
        rows.setAll(list);
    }

    public RunRow getSelected() { return table.getSelectionModel().getSelectedItem(); }

    public void appendRow(RunRow row) {
        rows.add(row);
        table.getSelectionModel().selectLast();
    }

    public void clearHistory() {
        rows.clear();
    }
}
