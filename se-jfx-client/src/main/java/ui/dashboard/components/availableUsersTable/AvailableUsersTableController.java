package ui.dashboard.components.availableUsersTable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.dashboard.UserDTO;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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
import java.util.concurrent.atomic.AtomicInteger;

import static util.support.Constants.GSON_INSTANCE;

public class AvailableUsersTableController extends AbstractRefreshableController {

    @FXML private TableView<UserDTO> usersTable;

    @FXML private TableColumn<UserDTO, String> colUserName;
    @FXML private TableColumn<UserDTO, Integer> colPrograms;
    @FXML private TableColumn<UserDTO, Integer> colFunctions;
    @FXML private TableColumn<UserDTO, Long> colCurrentCredits;
    @FXML private TableColumn<UserDTO, Long> colUsedCredits;
    @FXML private TableColumn<UserDTO, Integer> colExecutions;

    private final ObservableList<UserDTO> usersList = FXCollections.observableArrayList();

    private static final Type usersListType =
            TypeToken.getParameterized(List.class, UserDTO.class).getType();

    @FXML
    public void initialize() {
        colUserName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().userName()));
        colPrograms.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().numProgramsUploaded()));
        colFunctions.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().numFunctionsUploaded()));
        colCurrentCredits.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().currentCredits()));
        colUsedCredits.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().usedCredits()));
        colExecutions.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(cd.getValue().numOfExecutions()));

        usersTable.setItems(usersList);
    }

    @Override
    protected void fetchOnce() {
        fetchUsersOnce();
    }

    public void fetchUsersOnce() {
        String url = Constants.FULL_SERVER_PATH + "/users";

        HttpClientUtil.runAsync(url, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleNetworkFailure("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response; ResponseBody responseBody = response.body()) {
                    final String responseBodyString = responseBody != null ? responseBody.string() : "";

                    if (!response.isSuccessful()) {
                        int responseCode = response.code();
                        handleNetworkFailure("HTTP " + responseCode + " " + response.message() +
                                (responseBodyString.isBlank() ? "" : " | " + responseBodyString));
                        if (responseCode == 401 || responseCode == 403) {
                            handleUnauthorizedStop();
                        }
                        return;
                    }

                    List<UserDTO> incomingUsersDTOsList = GSON_INSTANCE.fromJson(responseBodyString, usersListType);
                    if (incomingUsersDTOsList == null) {
                        incomingUsersDTOsList = List.of();
                    }
                    resetFailures();
                    List<UserDTO> finalIncomingUsersDTOsList = incomingUsersDTOsList;
                    Platform.runLater(() -> {
                        replaceIfChanged(finalIncomingUsersDTOsList);
                    });
                }
            }
        });
    }

    private void replaceIfChanged(List<UserDTO> incomingUsersDTOsList) {
        super.replaceIfChanged(usersList, incomingUsersDTOsList);
    }

    @Override
    protected String logTag() {
        return "Users Poll";
    }
}
