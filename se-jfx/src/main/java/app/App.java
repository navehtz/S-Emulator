package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/ui/MainView.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        //scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/ui/styles.css")).toExternalForm());
        stage.setTitle("S-Emulator");
        stage.setScene(scene);
        stage.setMinWidth(300);
        stage.setMinHeight(200);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
