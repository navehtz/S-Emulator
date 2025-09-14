package ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DynamicInputsController {

    @FXML private VBox container;

    private final Map<String, Spinner<Integer>> fields = new LinkedHashMap<>();

    /** names like ["x1","x3","x5"] in display order */
    public void setInputs(List<String> inputNames) {
        container.getChildren().clear();
        fields.clear();
        for (String name : inputNames) {
            HBox row = new HBox(8);
            Label lab = new Label(name);
            Spinner<Integer> spin = new Spinner<>();
            spin.setEditable(true);
            spin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                    Integer.MIN_VALUE/4, Integer.MAX_VALUE/4, 0, 1));
            row.getChildren().addAll(lab, spin);
            container.getChildren().add(row);
            fields.put(name, spin);
        }
    }

    /** optional helper to prefill values (e.g., when re-run) */
    public void setValues(Map<String,Integer> values) {
        for (var e : values.entrySet()) {
            Spinner<Integer> sp = fields.get(e.getKey());
            if (sp != null) sp.getValueFactory().setValue(e.getValue());
        }
    }

    /** returns values in declared order */
    public Map<String,Integer> getValues() {
        Map<String,Integer> out = new LinkedHashMap<>();
        for (var e : fields.entrySet()) {
            out.put(e.getKey(), e.getValue().getValue());
        }
        return out;
    }
}
