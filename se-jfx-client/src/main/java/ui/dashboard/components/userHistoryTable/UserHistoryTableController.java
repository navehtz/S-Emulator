package ui.dashboard.components.userHistoryTable;

import com.google.gson.reflect.TypeToken;
import dto.dashboard.UserHistoryRowDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import util.http.HttpClientUtil;
import util.support.Constants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

import static util.support.Constants.GSON_INSTANCE;

public class UserHistoryTableController {

    private static final Type HISTORY_LIST_TYPE =
            TypeToken.getParameterized(List.class, UserHistoryRowDTO.class).getType();

    @FXML private TableView<UserHistoryRowDTO> historyTable;

    @FXML private TableColumn<UserHistoryRowDTO, Number> colRunNum;
    @FXML private TableColumn<UserHistoryRowDTO, String> colMainProgramOrFunction;
    @FXML private TableColumn<UserHistoryRowDTO, String> colProgramName;
    @FXML private TableColumn<UserHistoryRowDTO, String> colArchitectureType;
    @FXML private TableColumn<UserHistoryRowDTO, Number> colDegree;
    @FXML private TableColumn<UserHistoryRowDTO, Number> colCycles;
    @FXML private TableColumn<UserHistoryRowDTO, Number> colResult;

    @FXML public Button btnShow;
    @FXML public Button btnReRun;

//    public interface RerunListener { void onRerun(UserHistoryRowDTO row);}
//    private RerunListener rerunListener;
//
    private final ObservableList<UserHistoryRowDTO> tableRows = FXCollections.observableArrayList();
    private Consumer<UserHistoryRowDTO> showStatusHandler;

    @FXML
    private void initialize() {
        colRunNum.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().ordinal()));
        colMainProgramOrFunction.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().programType()));
        colProgramName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().operationName()));
        colArchitectureType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().architecture()));
        colDegree.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().degree()));
        colCycles.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().totalCycles()));
        colResult.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().result()));
        historyTable.setItems(tableRows);

        btnShow.disableProperty().bind(historyTable.getSelectionModel().selectedItemProperty().isNull());
        btnReRun.disableProperty().bind(historyTable.getSelectionModel().selectedItemProperty().isNull());
    }

    public void setRows(List<UserHistoryRowDTO> list) {
        tableRows.setAll(list);
    }

    public UserHistoryRowDTO getSelectedUserHistoryRowDTO() {
        return historyTable.getSelectionModel().getSelectedItem();
    }

    public int getSelectedIndex() {
        return historyTable.getSelectionModel().getSelectedIndex();
    }

    public void appendRow(UserHistoryRowDTO row) {
        tableRows.add(row);
        historyTable.getSelectionModel().selectLast();
    }

    public void clearHistory() {
        tableRows.clear();
    }

    public void replaceAll(List<UserHistoryRowDTO> tableRows) {
        historyTable.getItems().setAll(tableRows);
    }

    public void setOnShowStatus(Consumer<UserHistoryRowDTO> handler) {
        this.showStatusHandler = handler;
    }

    @FXML
    private void onShowStatus() {
        UserHistoryRowDTO selectedRow = historyTable.getSelectionModel().getSelectedItem();
        if (selectedRow == null) return;

        if (showStatusHandler != null) {
            showStatusHandler.accept(selectedRow);
        }
    }

    public void showHistoryForUser(String username) {
        String url = Constants.USER_HISTORY + "?" + Constants.USER_NAME_QUERY_PARAM + "=" + username;

        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("Failed to fetch history for " + username + ": " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response; ResponseBody body = response.body()) {
                    if (!response.isSuccessful() || body == null) return;
                    List<UserHistoryRowDTO> rows = GSON_INSTANCE.fromJson(body.string(), HISTORY_LIST_TYPE);
                    if (rows == null) rows = List.of();
                    List<UserHistoryRowDTO> finalRows = rows;
                    Platform.runLater(() -> setRows(finalRows));
                }
            }
        });
    }

    public void setOnRerun(/*RerunListener rerunListener*/) {
        /*this.rerunListener = rerunListener;*/
    }

    @FXML
    private void onReRun() {
//        UserHistoryRowDTO selectedRow = historyTable.getSelectionModel().getSelectedItem();
//        if (selectedRow != null && rerunListener != null)
//            rerunListener.onRerun(selectedRow);
    }
}
