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
import ui.execution.components.main.ExecutionPageController;
import ui.login.LoginController;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

import static util.support.Constants.*;

public class SEmulatorAppMainController implements Closeable {

    private GridPane loginComponent;
    private LoginController loginController;

    private Parent dashboardComponent;
    private DashboardController dashboardComponentController;
    private Parent executionComponent;
    private ExecutionPageController executionPageController;

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
        if (pane == null) {
            System.err.println("setMainPanelTo called with null pane (FXML failed to load)");
            return;
        }

        mainPanel.getChildren().setAll(pane);

        AnchorPane.setTopAnchor(pane, 0.0);
        AnchorPane.setBottomAnchor(pane, 0.0);
        AnchorPane.setLeftAnchor(pane, 0.0);
        AnchorPane.setRightAnchor(pane, 0.0);
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

        if (executionPageController != null) {
            executionPageController.onBecameInactive();
        }

        Platform.runLater(() -> {
            var scene = mainPanel.getScene();
            if (scene != null && scene.getWindow() instanceof Stage stage) {
                stage.sizeToScene();
                stage.centerOnScreen();
            }
        });
    }

    public void loadExecutionPage() throws IOException {
        if (executionComponent == null) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(EXECUTION_PAGE_FXML_RESOURCE_LOCATION));
            executionComponent = fxmlLoader.load();
            executionPageController = fxmlLoader.getController();
            executionPageController.setSEmulatorAppMainController(this);
            executionPageController.bindUserName(currentUserName);

            //setMainPanelTo(executionComponent);
        }
    }

    public void switchToExecutionPage() {
        Platform.runLater(() -> {
            dashboardComponentController.setInActive();
            try {
                loadExecutionPage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void switchToExecutionPage(String programName) {
        Platform.runLater(() -> {
            if (dashboardComponentController != null) {
                dashboardComponentController.setInActive();
            }
            try {
                loadExecutionPage();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            setMainPanelTo(executionComponent);

            if (executionPageController != null && programName != null) {
                executionPageController.onBecameActive();
                executionPageController.loadProgramForExecution(programName);
            }
        });
    }

    public void switchToLogin() {
        Platform.runLater(() -> {
            dashboardComponentController.setInActive();
            setMainPanelTo(loginComponent);
        });
    }
}
