package main.java.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.MainController;

import java.util.Objects;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/MainView.fxml"));
        Parent root = loader.load();
        MainController mainController = loader.getController();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/ui/styles/vars.css")).toExternalForm());
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
