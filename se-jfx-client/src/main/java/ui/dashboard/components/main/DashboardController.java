package ui.dashboard.components.main;

import javafx.fxml.FXML;
import ui.dashboard.components.availableFunctionsTable.AvailableFunctionsTableController;
import ui.dashboard.components.availableProgramsTable.AvailableProgramsTableController;
import ui.dashboard.components.availableUsersTable.AvailableUsersTableController;
import ui.dashboard.components.topBar.TopBarController;
import ui.dashboard.components.userHistoryTable.UserHistoryTableController;
import ui.main.components.SEmulatorAppMainController;

import java.io.Closeable;
import java.io.IOException;

public class DashboardController implements Closeable {

    @FXML TopBarController topBarController;
    @FXML AvailableProgramsTableController availableProgramsTableController;
    @FXML AvailableFunctionsTableController availableFunctionsTableController;
    @FXML AvailableUsersTableController availableUsersTableController;
    @FXML UserHistoryTableController userHistoryTableController;


    private SEmulatorAppMainController sEmulatorAppMainController;



    @FXML
    private void initialize() { }


    @Override
    public void close() throws IOException {
        // TODO
        //availableUsersTableController.close();
    }

    public void setActive() {
        // TODO
    }

    public void setInActive() {
        try {
            // TODO
        } catch (Exception ignored) {}
    }

    public void setSEmulatorAppMainController(SEmulatorAppMainController sEmulatorAppMainController) {
        this.sEmulatorAppMainController = sEmulatorAppMainController;
    }

}
