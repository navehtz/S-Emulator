package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ui.dashboard.components.main.DashboardController;

import java.util.Objects;

import static util.support.Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(MAIN_PAGE_FXML_RESOURCE_LOCATION));
        Parent root = loader.load();
        //DashboardController dashboardController = loader.getController();
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
