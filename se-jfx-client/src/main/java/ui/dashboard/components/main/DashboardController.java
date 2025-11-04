package ui.dashboard.components.main;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import ui.dashboard.components.availableFunctionsTable.AvailableFunctionsTableController;
import ui.dashboard.components.availableProgramsTable.AvailableProgramsTableController;
import ui.dashboard.components.availableUsersTable.AvailableUsersTableController;
import ui.dashboard.components.topBar.TopBarController;
import ui.dashboard.components.userHistoryTable.UserHistoryTableController;
import ui.main.components.SEmulatorAppMainController;
import util.support.Constants;

import javax.swing.border.TitledBorder;
import java.io.Closeable;
import java.io.IOException;

public class DashboardController implements Closeable {

    @FXML TopBarController topBarController;
    @FXML AvailableProgramsTableController programsTableController;
    @FXML AvailableFunctionsTableController functionsTableController;
    @FXML AvailableUsersTableController availableUsersTableController;
    @FXML UserHistoryTableController userHistoryTableController;


    private SEmulatorAppMainController sEmulatorAppMainController;



    @FXML
    private void initialize() {
        programsTableController.setOnExecuteProgram(row -> {
            if (sEmulatorAppMainController != null && row != null && !row.programName().isBlank()) {
                sEmulatorAppMainController.switchToExecutionPage(row.programName());
            }
        });

        functionsTableController.setOnExecuteFunction(row -> {
            if (sEmulatorAppMainController != null && row != null && !row.functionName().isBlank()) {
                sEmulatorAppMainController.switchToExecutionPage(row.functionName());
            }
        });

        topBarController.setOnChargeCredits(() -> availableUsersTableController.refreshNow());
    }

    public void bindUserName(StringProperty userNameProperty) {
        topBarController.userNameProperty().bind(userNameProperty);
    }

    @Override
    public void close() throws IOException {
        // TODO
        //availableUsersTableController.close();
    }

    public void setActive() {
        // TODO
        availableUsersTableController.startAutoRefresh(Constants.REFRESH_RATE);
        programsTableController.startAutoRefresh(Constants.REFRESH_RATE);
        functionsTableController.startAutoRefresh(Constants.REFRESH_RATE);
    }

    public void setInActive() {
        try {
            // TODO
            availableUsersTableController.stopAutoRefresh();
            programsTableController.stopAutoRefresh();
            functionsTableController.stopAutoRefresh();
        } catch (Exception ignored) {}
    }

    public void setSEmulatorAppMainController(SEmulatorAppMainController sEmulatorAppMainController) {
        this.sEmulatorAppMainController = sEmulatorAppMainController;
    }

}
