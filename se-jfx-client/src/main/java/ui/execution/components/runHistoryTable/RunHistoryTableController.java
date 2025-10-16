package ui.execution.components.runHistoryTable;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.function.Consumer;

public class RunHistoryTableController {


    public record RunRow(int runNum, String programKey, int degree, List<Long> inputs, long result, int cycles) {
    }

    @FXML private TableView<RunRow> table;
    @FXML private TableColumn<RunRow, Number> colRunNum;
    @FXML private TableColumn<RunRow, Number> colDegree;
    @FXML private TableColumn<RunRow, Number> colResult;
    @FXML private TableColumn<RunRow, Number> colCycles;

    @FXML public Button btnShow;
    @FXML public Button btnRerun;

    public interface RerunListener { void onRerun(RunRow row);}
    private RerunListener rerunListener;

    private final ObservableList<RunRow> rows = FXCollections.observableArrayList();
    private Consumer<RunRow> showStatusHandler;

    @FXML
    private void initialize() {
        colRunNum.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().runNum));
        colDegree.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().degree));
        colResult.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().result));
        colCycles.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().cycles));
        table.setItems(rows);

        btnShow.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        btnRerun.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
    }

    public void setRows(List<RunRow> list) {
        rows.setAll(list);
    }

    public RunRow getSelectedRunRow() {
        return table.getSelectionModel().getSelectedItem();
    }

    public int getSelectedIndex() {
        return table.getSelectionModel().getSelectedIndex();
    }

    public void appendRow(RunRow row) {
        rows.add(row);
        table.getSelectionModel().selectLast();
    }

    public void clearHistory() {
        rows.clear();
    }

    public void replaceAll(List<RunRow> rows) {
        table.getItems().setAll(rows);
    }

    public void setOnShowStatus(Consumer<RunRow> handler) {
        this.showStatusHandler = handler;
    }

    @FXML
    private void onShowStatus() {
        RunRow selectedRow = table.getSelectionModel().getSelectedItem();
        if (selectedRow == null) return;

        if (showStatusHandler != null) {
            showStatusHandler.accept(selectedRow);
        }
    }

    public void setOnRerun(RerunListener rerunListener) {
        this.rerunListener = rerunListener;
    }

    @FXML
    private void onRerun() {
        RunRow selectedRow = table.getSelectionModel().getSelectedItem();
        if (selectedRow != null && rerunListener != null)
            rerunListener.onRerun(selectedRow);
    }
}
