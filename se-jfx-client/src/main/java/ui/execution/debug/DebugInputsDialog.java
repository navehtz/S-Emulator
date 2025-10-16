package ui.execution.debug;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DebugInputsDialog extends Dialog<Map<String, Double>> {
    private final Map<String, Spinner<Double>> inputSpinners = new LinkedHashMap<>();

    public DebugInputsDialog(
            javafx.stage.Window ownerWindow,
            List<String> requiredInputs,
            Map<String, Double> prefill // may be null
    ) {
        setTitle("Debug Inputs");
        setHeaderText("Enter values for the required inputs");
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        if (ownerWindow != null) initOwner(ownerWindow);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(12));

        int row = 0;
        for (String name : requiredInputs) {
            Label label = new Label(name + ":");
            Spinner<Double> spinner = new Spinner<>(0.0, Double.MAX_VALUE, 0, 1);
            spinner.setEditable(true);

            // prefill if provided, else zero
            Double initial = (prefill != null && prefill.containsKey(name)) ? prefill.get(name) : 0L;
            spinner.getValueFactory().setValue(initial);
            spinner.getEditor().setOnMouseClicked(event -> {
                        if (!spinner.getEditor().isFocused()) {
                            spinner.getEditor().requestFocus();
                        }
                        spinner.getEditor().selectAll();
                    });

            // commit typed value when focus leaves
            spinner.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    spinner.getEditor().selectAll();
                } else {
                    try {
                        spinner.increment(0); }
                    catch (Exception ignore) {}
                }
            });

            inputSpinners.put(name, spinner);
            grid.add(label,   0, row);
            grid.add(spinner, 1, row++);
        }

        // Basic validation: if any editor has non-integer text → disable OK
        Node okButton = getDialogPane().lookupButton(ButtonType.OK);
        okButton.disableProperty().bind(new javafx.beans.binding.BooleanBinding() {
            {
                inputSpinners.values().forEach(s -> super.bind(s.getEditor().textProperty()));
            }
            @Override protected boolean computeValue() {
                for (Spinner<Double> s : inputSpinners.values()) {
                    String t = s.getEditor().getText();
                    if (t == null || t.isBlank()) continue; // blank → treat as 0, allowed
                    try { Long.parseLong(t.trim()); } catch (NumberFormatException e) { return true; }
                }
                return false;
            }
        });

        getDialogPane().setContent(new ScrollPane(grid) {{
            setFitToWidth(true);
            setPrefViewportHeight(Math.min(360, 48 + requiredInputs.size() * 36));
        }});

        setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            // Ensure latest text is committed
            inputSpinners.values().forEach(s -> { try { s.increment(0); } catch (Exception ignore) {} });
            return inputSpinners.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().getValue(),
                            (a, b) -> a,
                            LinkedHashMap::new
                    ));
        });
    }
}
