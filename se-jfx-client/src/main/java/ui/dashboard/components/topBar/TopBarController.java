package ui.dashboard.components.topBar;

import dto.dashboard.UserDTO;
import dto.execution.ProgramDTO;
import exceptions.EngineLoadException;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import util.http.HttpClientUtil;
import util.support.Dialogs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static util.support.Constants.*;
import static util.support.Helpers.validatePath;

public class TopBarController {

    @FXML private Label programNameLabel;
    @FXML private Button btnLoadFile;
    @FXML private Label userNameLabel;
    @FXML private Label availableCreditsLabel;
    @FXML private Button btnChargeCredits;

    private SimpleStringProperty userNameProperty = new SimpleStringProperty();
    private Runnable onChargeCredits;

    public void setOnChargeCredits(Runnable runnable) {
        this.onChargeCredits = runnable;
    }

    @FXML
    public void initialize() {
        userNameLabel.textProperty().bind(userNameProperty);

        refreshCreditsFromServer();
    }

    public void setUserName(String userName) {
        userNameProperty.set(userName);
    }

    public StringProperty userNameProperty() {
        return userNameProperty;
    }

    public void onLoadXml(ActionEvent actionEvent) throws EngineLoadException, IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Program XML");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("xml files", "*.xml"),
                new FileChooser.ExtensionFilter("all files", "*.*")
        );

        final Window window = btnLoadFile.getScene().getWindow();
        final File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile == null) {
            Dialogs.warning("No file selected", "Please choose an XML file.", window);
            return;
        }

        final String name = selectedFile.getName().toLowerCase();

        if (!(name.endsWith(".xml"))) {
            Dialogs.warning("Invalid File", "Please choose an XML file.", window);
            return;
        }

        programNameLabel.setText(selectedFile.getAbsolutePath());

        final Path programPath = Path.of(selectedFile.getAbsolutePath()).toAbsolutePath().normalize();
        validatePath(programPath);

        //OkHttpClient client = new OkHttpClient();

        MediaType mediaType = MediaType.parse("application/xml");
        RequestBody requestBody = RequestBody.create(selectedFile, mediaType);

        Request request = new Request.Builder()
                .url(FULL_SERVER_PATH + "/loadProgram")
                .post(requestBody)
                .build();

        try ( Response response = HttpClientUtil.HTTP_CLIENT.newCall(request).execute() ) {
            String responseBody = response.body() != null ? response.body().string() : "";
            String contentType = response.header("Content-Type", "");

            if (!response.isSuccessful()) {
                Dialogs.error("Load failed", response.code() + " " + response.message() + "\n" + responseBody, getOwnerWindowOrNull());
                return;
            }

            if (!contentType.contains("application/json") || responseBody.isBlank() || responseBody.charAt(0) != '{') {
                throw new IOException("Expected JSON object but got: " + contentType + " ; body starts with: " +
                        (responseBody.length() > 60 ? responseBody.substring(0,60) + "..." : responseBody));
            }

            ProgramDTO baseProgram = GSON_INSTANCE.fromJson(responseBody, ProgramDTO.class);
        }
    }

    private Window getOwnerWindowOrNull() {
        return (btnLoadFile.getScene() != null) ? btnLoadFile.getScene().getWindow() : null;
    }

    @FXML
    private void onChargeCredits() {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setTitle("Charge Credits");
        textInputDialog.setHeaderText("Enter the number of credits to charge");
        textInputDialog.setContentText("Credits:");

        TextField textField = textInputDialog.getEditor();
        handleTextField(textField);
        Button okButton = (Button) textInputDialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(textField.textProperty().isEmpty());

        Platform.runLater(textField::requestFocus);

        Optional<String> result = textInputDialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        String creditsStr = result.get();
        if (creditsStr.isEmpty()) {
            return;
        }

        long amountOfCredits = Long.parseLong(creditsStr);
        sendChargeCreditsRequest(amountOfCredits);
    }

    private void handleTextField(TextField textField) {
        int maxLen = 18;
        textField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();

            if (!newText.matches("\\d*")) {
                return null;
            }

            if (newText.length() > maxLen) {
                return null;
            }

            if (newText.startsWith("0") && newText.length() > 1) {
                return null;
            }

            if (!newText.isEmpty()) {
                try {
                    Long.parseLong(newText);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return change;
        }));
    }

    private void sendChargeCreditsRequest(long amountOfCredits) {
        String url = FULL_SERVER_PATH + "/credits?" + CREDITS_AMOUNT_QUERY_PARAM + "=" + amountOfCredits;

        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(null, new byte[0]))
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        Dialogs.error("Charge failed", e.getMessage(), availableCreditsLabel.getScene().getWindow()));
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response; ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        Platform.runLater(() ->
                                Dialogs.error("Charge failed", response.code() + " " + response.message(), availableCreditsLabel.getScene().getWindow()));
                        return;
                    }

                    UserDTO userDTO = GSON_INSTANCE.fromJson(responseBody.string(), UserDTO.class);
                    if (userDTO == null) {
                        Platform.runLater(() ->
                                Dialogs.error("Charge failed", "Invalid response body", availableCreditsLabel.getScene().getWindow()));
                    }
                    Platform.runLater(() -> {
                        assert userDTO != null;
                        availableCreditsLabel.setText(String.valueOf(userDTO.currentCredits()));
                    });
                }
            }
        });
    }

    public void refreshCreditsFromServer() {
        String url = FULL_SERVER_PATH + "/credits";

        HttpClientUtil.runAsync(url, new Callback() {
            @Override public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.err.println("[TopBar] credits fetch failed: " + e.getMessage());
            }
            @Override public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try (response; ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) return;
                    String json = responseBody != null ? responseBody.string() : "";
                    UserDTO user = GSON_INSTANCE.fromJson(json, UserDTO.class);
                    Platform.runLater(() -> availableCreditsLabel.setText(String.valueOf(user.currentCredits())));
                }
            }
        });
    }
}


