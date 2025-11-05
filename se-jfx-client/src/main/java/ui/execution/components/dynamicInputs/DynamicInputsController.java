package ui.execution.components.dynamicInputs;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class DynamicInputsController {

    @FXML private VBox container;

    public void setInputsValuesOfUser(List<Long> inputsValues) {
        container.getChildren().clear();
        for (int i = 0; i < inputsValues.size(); i++) {
            HBox row = new HBox(8);
            Label label = new Label("x" + (i + 1) + ": ");
            Label value = new Label(inputsValues.get(i).toString());
            row.getChildren().addAll(label, value);
            container.getChildren().add(row);
        }
    }

    public void clearInputs() {
        container.getChildren().clear();
    }

}
