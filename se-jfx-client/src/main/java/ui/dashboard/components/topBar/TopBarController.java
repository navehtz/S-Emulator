package ui.dashboard.components.topBar;

import com.google.gson.Gson;
import dto.ProgramDTO;
import exceptions.EngineLoadException;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import okhttp3.*;
import util.support.Dialogs;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static util.support.Helpers.validatePath;

public class TopBarController {

    @FXML private Label programNameLabel;
    @FXML private Button btnLoadFile;

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

        OkHttpClient client = new OkHttpClient();
        String BASE_URL = "http://localhost:8080" + "/S-Emulator_App_Web"; //TODO: consts

        MediaType mediaType = MediaType.parse("application/xml");
        RequestBody requestBody = RequestBody.create(selectedFile, mediaType);

        Request request = new Request.Builder()
                .url(BASE_URL + "/loadProgram")
                .post(requestBody)
                .build();

        try ( Response response = client.newCall(request).execute() ) {
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


