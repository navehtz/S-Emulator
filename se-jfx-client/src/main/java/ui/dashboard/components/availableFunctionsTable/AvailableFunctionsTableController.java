package ui.dashboard.components.availableFunctionsTable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.dashboard.AvailableFunctionDTO;
import dto.dashboard.AvailableProgramDTO;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import ui.dashboard.components.refreshables.AbstractRefreshableController;
import util.http.HttpClientUtil;
import util.support.Constants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Consumer;

import static util.support.Constants.GSON_INSTANCE;

public class AvailableFunctionsTableController extends AbstractRefreshableController {

    @FXML private TableView<AvailableFunctionDTO> functionsTable;

    @FXML private TableColumn<AvailableFunctionDTO, String> colFunctionName;
    @FXML private TableColumn<AvailableFunctionDTO, String>  colProgramName;
    @FXML private TableColumn<AvailableFunctionDTO, String>  colUserUploaded;
    @FXML private TableColumn<AvailableFunctionDTO, Integer> colNumInstructions;
    @FXML private TableColumn<AvailableFunctionDTO, Integer> colMaxDegree;
    @FXML private Button btnExecuteFunction;

    private Consumer<AvailableFunctionDTO> executeFunctionHandler;

    private final ObservableList<AvailableFunctionDTO> functionsList = FXCollections.observableArrayList();
    private static final Type LIST_TYPE =
            TypeToken.getParameterized(List.class, AvailableFunctionDTO.class).getType();

    @Override protected String logTag() { return "Functions Poll"; }

    @FXML
    public void initialize() {
        colFunctionName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().functionName()));
        colProgramName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().mainProgramName()));
        colUserUploaded.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().userUploaded()));
        colNumInstructions.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().numOfInstructions()));
        colMaxDegree.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().maxDegree()));

        functionsTable.setItems(functionsList);

        btnExecuteFunction.disableProperty().bind(functionsTable.getSelectionModel().selectedItemProperty().isNull());
    }

    @Override
    protected void fetchOnce() {
        String url = Constants.FULL_SERVER_PATH + "/functions";
        HttpClientUtil.runAsync(url, new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleNetworkFailure("Network error: " + e.getMessage());
            }

            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response; ResponseBody responseBody = response.body()) {
                    String responseBodyString = responseBody != null ? responseBody.string() : "";
                    if (!response.isSuccessful()) {
                        if (response.code() == 401 || response.code() == 403) handleUnauthorizedStop();
                        handleNetworkFailure("HTTP " + response.code() + " " + response.message()
                                + (responseBodyString.isBlank() ? "" : " | " + responseBodyString));
                        return;
                    }
                    List<AvailableFunctionDTO> incomingFunctions = GSON_INSTANCE.fromJson(responseBodyString, LIST_TYPE);
                    resetFailures();
                    Platform.runLater(() -> replaceIfChanged(functionsList, incomingFunctions));
                }
            }
        });
    }

    @FXML private void btnExecuteFunctionClicked(ActionEvent event) {
        AvailableFunctionDTO selectedRow = functionsTable.getSelectionModel().getSelectedItem();
        if (selectedRow == null) return;

        if (executeFunctionHandler != null) {
            executeFunctionHandler.accept(selectedRow);
        }
    }


    public void setOnExecuteFunction(Consumer<AvailableFunctionDTO> handler) {
        this.executeFunctionHandler = handler;
    }
}
