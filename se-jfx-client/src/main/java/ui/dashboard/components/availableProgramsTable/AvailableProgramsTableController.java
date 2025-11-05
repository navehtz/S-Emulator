package ui.dashboard.components.availableProgramsTable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import ui.execution.components.runHistoryTable.RunHistoryTableController;
import util.http.HttpClientUtil;
import util.support.Constants;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static util.support.Constants.GSON_INSTANCE;

public class AvailableProgramsTableController extends AbstractRefreshableController {

    @FXML private TableView<AvailableProgramDTO> programsTable;

    @FXML private TableColumn<AvailableProgramDTO, String>  colProgramName;
    @FXML private TableColumn<AvailableProgramDTO, String>  colUserUploaded;
    @FXML private TableColumn<AvailableProgramDTO, Integer> colNumInstructions;
    @FXML private TableColumn<AvailableProgramDTO, Integer> colMaxDegree;
    @FXML private TableColumn<AvailableProgramDTO, Integer> colNumExecutions;
    @FXML private TableColumn<AvailableProgramDTO, Integer> colAverageCreditCost;

    @FXML private Button btnExecuteProgram;

    private Consumer<AvailableProgramDTO> executeProgramHandler;
//    public record ProgramRow(String name, String user, int instructions, int naxDegree, int executions, int averageCost) {
//    }

    private final ObservableList<AvailableProgramDTO> programsList = FXCollections.observableArrayList();

    private static final Type PROGRAMS_LIST_TYPE =
                        TypeToken.getParameterized(List.class, AvailableProgramDTO.class).getType();

    @Override
    protected String logTag() {
        return "Programs Poll";
    }

    @FXML
    public void initialize() {
        colProgramName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().programName()));
        colUserUploaded.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().userUploaded()));
        colNumInstructions.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().numOfInstructions()));
        colMaxDegree.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().maxDegree()));
        colNumExecutions.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().numOfExecutions()));
        colAverageCreditCost.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().averageCreditCost()));

        programsTable.setItems(programsList);

        btnExecuteProgram.disableProperty().bind(programsTable.getSelectionModel().selectedItemProperty().isNull());
//        btnExecuteProgram.setOnAction(event -> {
//            var selectedRow = programsTable.getSelectionModel().getSelectedItem();
//            if (selectedRow != null) {
//                // TODO: route to execute page
//                System.out.println("Execute program: " + selectedRow.programName());
//            }
//        });
    }

    @Override
    protected void fetchOnce() {
        String url = Constants.FULL_SERVER_PATH + "/programs";
        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleNetworkFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response; ResponseBody responseBody = response.body()) {
                    String responseBodyString = responseBody != null ? responseBody.string() : "";
                    if (!response.isSuccessful()) {
                        int responseCode = response.code();
                        if (responseCode == 401 || responseCode == 403) {
                            handleUnauthorizedStop();
                        }
                        handleNetworkFailure("HTTP " + responseCode + " " + response.message() +
                                (responseBodyString.isBlank() ? "" : " | " + responseBodyString));
                        return;
                    }
                    List<AvailableProgramDTO> incomingPrograms = GSON_INSTANCE.fromJson(responseBodyString, PROGRAMS_LIST_TYPE);
                    resetFailures();
                    Platform.runLater(() -> replaceIfChanged(programsList, incomingPrograms));
                }
            }
        });
    }

    @FXML private void btnExecuteProgramClicked(ActionEvent event) {
        AvailableProgramDTO selectedRow = programsTable.getSelectionModel().getSelectedItem();
        if (selectedRow == null) return;

        if (executeProgramHandler != null) {
            executeProgramHandler.accept(selectedRow);
        }
    }


    public void setOnExecuteProgram(Consumer<AvailableProgramDTO> handler) {
        this.executeProgramHandler = handler;
    }

}
