package ui.dashboard.components.main;

import javafx.fxml.FXML;
import ui.dashboard.components.availableFunctionsTable.AvailableFunctionsTableController;
import ui.dashboard.components.availableProgramsTable.AvailableProgramsTableController;
import ui.dashboard.components.availableUsersTable.AvailableUsersTableController;
import ui.dashboard.components.topBar.TopBarController;
import ui.dashboard.components.userHistoryTable.UserHistoryTableController;

public class DashboardController {

    @FXML TopBarController topBarController;
    @FXML AvailableProgramsTableController availableProgramsTableController;
    @FXML AvailableFunctionsTableController availableFunctionsTableController;
    @FXML AvailableUsersTableController availableUsersTableController;
    @FXML UserHistoryTableController userHistoryTableController;

    @FXML
    private void initialize() { }

}
