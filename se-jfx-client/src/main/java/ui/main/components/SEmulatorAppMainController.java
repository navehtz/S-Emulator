package ui.main.components;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import ui.dashboard.components.main.DashboardController;
import ui.login.LoginController;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import static util.support.Constants.*;

public class SEmulatorAppMainController implements Closeable {

    private GridPane loginComponent;
    private LoginController loginController;

    private Parent dashboardComponent;
    private DashboardController dashboardComponentController;

    @FXML private AnchorPane mainPanel;

    private final StringProperty currentUserName;

    public SEmulatorAppMainController() {
        currentUserName = new SimpleStringProperty(TEMP_NAME);
    }

    @FXML
    public void initialize() {
        loadLoginPage();
        loadDashboardPage();
    }

    public void updateUserName(String userName) {
        currentUserName.set(userName);
    }
    
    private void setMainPanelTo(Parent pane) {
        AnchorPane.clearConstraints(pane);

        mainPanel.getChildren().setAll(pane);

        pane.applyCss();
        pane.autosize();

        double prefWidth = pane.prefWidth(-1);
        double prefHeight = pane.prefHeight(-1);
        if (!Double.isNaN(prefWidth)) mainPanel.setPrefWidth(prefWidth);
        if (!Double.isNaN(prefHeight)) mainPanel.setPrefHeight(prefHeight);

        Scene scene = mainPanel.getScene();
        if (scene != null && scene.getWindow() instanceof Stage stage) {
            Platform.runLater(() -> {
                pane.applyCss();
                pane.autosize();
                stage.sizeToScene();
            });
        }
    }

    @Override
    public void close() throws IOException {
        dashboardComponentController.close();
    }

    private void loadLoginPage() {
        URL loginPageUrl = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            loginController = fxmlLoader.getController();
            loginController.setSEmulatorAppMainController(this);
            setMainPanelTo(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDashboardPage() {
        URL dasboardPageUrl = getClass().getResource(DASHBOARD_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(dasboardPageUrl);
            dashboardComponent = fxmlLoader.load();
            dashboardComponentController = fxmlLoader.getController();
            dashboardComponentController.setSEmulatorAppMainController(this);

            dashboardComponentController.bindUserName(currentUserName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void switchToDashboard() {
        setMainPanelTo(dashboardComponent);
        dashboardComponentController.setActive();
    }

    public void switchToLogin() {
        Platform.runLater(() -> {
            dashboardComponentController.setInActive();
            setMainPanelTo(loginComponent);
        });
    }
}
