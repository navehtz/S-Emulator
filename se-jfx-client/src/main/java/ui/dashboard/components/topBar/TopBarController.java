package ui.dashboard.components.topBar;

import com.google.gson.Gson;
import dto.execution.ProgramDTO;
import exceptions.EngineLoadException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import okhttp3.*;
import util.http.HttpClientUtil;
import util.support.Dialogs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static util.support.Constants.FULL_SERVER_PATH;
import static util.support.Helpers.validatePath;

public class TopBarController {

    @FXML private Label programNameLabel;
    @FXML private Button btnLoadFile;
    @FXML private Label userNameLabel;

    private SimpleStringProperty userNameProperty = new SimpleStringProperty();

    @FXML
    public void initialize() {
        userNameLabel.textProperty().bind(userNameProperty);
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

            Gson gson = new Gson();
            ProgramDTO baseProgram = gson.fromJson(responseBody, ProgramDTO.class);
        }
    }

    private Window getOwnerWindowOrNull() {
        return (btnLoadFile.getScene() != null) ? btnLoadFile.getScene().getWindow() : null;
    }
}


