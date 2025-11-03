package ui.execution.components.topBar;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class TopBarController {

    @FXML private Button btnBackToDashboard;
    @FXML private Label userNameLabel;

    private SimpleStringProperty userNameProperty = new SimpleStringProperty();

    @FXML
    public void initialize() {
        userNameLabel.textProperty().bind(userNameProperty);
    }

    public StringProperty userNameProperty() {
        return userNameProperty;
    }

    @FXML
    private void backToDashboardClicked(ActionEvent event) {
        // TODO
    }
}
