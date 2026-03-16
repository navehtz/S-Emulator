package ui.dashboard.components.main;

import dto.dashboard.UserHistoryRowDTO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import ui.dashboard.components.availableFunctionsTable.AvailableFunctionsTableController;
import ui.dashboard.components.availableProgramsTable.AvailableProgramsTableController;
import ui.dashboard.components.availableUsersTable.AvailableUsersTableController;
import ui.dashboard.components.topBar.TopBarController;
import ui.dashboard.components.userHistoryTable.UserHistoryTableController;
import ui.main.components.SEmulatorAppMainController;
import util.support.Constants;
import util.support.Dialogs;

import java.io.Closeable;
import java.io.IOException;

public class DashboardController implements Closeable {

    @FXML TopBarController topBarController;
    @FXML AvailableProgramsTableController programsTableController;
    @FXML AvailableFunctionsTableController functionsTableController;
    @FXML AvailableUsersTableController availableUsersTableController;
    @FXML UserHistoryTableController userHistoryTableController;


    private SEmulatorAppMainController sEmulatorAppMainController;
    private StringProperty rawUserNameProperty = new SimpleStringProperty();



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

        availableUsersTableController.setOnUserSelected(selectedUsername -> {
            String target = selectedUsername != null
                    ? selectedUsername
                    : rawUserNameProperty.get();
            userHistoryTableController.showHistoryForUser(target);
        });

        userHistoryTableController.setOnShowStatus(this::showStatusPopup);

        userHistoryTableController.setOnRerun(row -> {
            if (sEmulatorAppMainController != null) {
                sEmulatorAppMainController.switchToExecutionPageForRerun(
                        row.operationName(), row.degree(), row.inputsValuesOfUser());
            }
        });
    }

    public void bindUserName(StringProperty userNameProperty) {
        topBarController.userNameProperty().bind(userNameProperty);
    }

    public void bindRawUserName(StringProperty rawUserName) {
        rawUserNameProperty.bind(rawUserName);
    }

    @Override
    public void close() throws IOException {
        //availableUsersTableController.close();
    }

    public void setActive() {
        availableUsersTableController.startAutoRefresh(Constants.REFRESH_RATE);
        programsTableController.startAutoRefresh(Constants.REFRESH_RATE);
        functionsTableController.startAutoRefresh(Constants.REFRESH_RATE);
        topBarController.refreshCreditsFromServer();

        String username = rawUserNameProperty.get();
        if (username != null && !username.isBlank()) {
            userHistoryTableController.showHistoryForUser(username);
        }
    }

    public void setInActive() {
        try {
            availableUsersTableController.stopAutoRefresh();
            programsTableController.stopAutoRefresh();
            functionsTableController.stopAutoRefresh();
        } catch (Exception ignored) {}
    }

    private void showStatusPopup(UserHistoryRowDTO row) {
        StringBuilder statusMessage = new StringBuilder("Final variable values:\n\n");
        statusMessage.append("Result = ").append(row.result()).append("\n");
        row.variablesToValuesSorted().forEach((variableName, variableValue) ->
                statusMessage.append(variableName).append(" = ").append(variableValue).append("\n")
        );
        Dialogs.info("Program Status", statusMessage.toString(), null);
    }

    public void setSEmulatorAppMainController(SEmulatorAppMainController sEmulatorAppMainController) {
        this.sEmulatorAppMainController = sEmulatorAppMainController;
    }

}
