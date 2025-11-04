package ui.execution.components.topBar;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import util.themes.Theme;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;


public class TopBarController {

    private static final String PREF_KEY_THEME = "app.theme";
    @FXML private Button btnBackToDashboard;
    @FXML private Label userNameLabel;
    @FXML private ComboBox<Integer> degreeSelector;
    @FXML private ComboBox<String> highlightSelector;
    @FXML private ComboBox<String> themeSelector;


    private SimpleStringProperty userNameProperty = new SimpleStringProperty();

    private Runnable onBackToDashboard;
    private IntConsumer onDegreeChanged;
    private final List<TableView<?>> themedTables = new ArrayList<>();


    @FXML
    public void initialize() {
        userNameLabel.textProperty().bind(userNameProperty);
        degreeSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && onDegreeChanged != null) {
                onDegreeChanged.accept(newVal);
            }
        });
        handleTheme();
    }

    public StringProperty userNameProperty() {
        return userNameProperty;
    }

    public void setOnBackToDashboard(Runnable runnable) {
        this.onBackToDashboard = runnable;
    }

    public void setOnDegreeChanged(IntConsumer consumer) {
        this.onDegreeChanged = consumer;
    }

    public void setDegrees(List<Integer> degrees) {
        degreeSelector.getItems().setAll(degrees);
    }

    public void selectDegree(int degree) {
        degreeSelector.getSelectionModel().select(Integer.valueOf(degree));
    }

    public void setHighlights(List<String> highlights) {
        highlightSelector.getItems().setAll(highlights);
    }

    public void selectHighlight(String value) {
        if (value == null) {
            highlightSelector.getSelectionModel().clearSelection();
        } else {
            highlightSelector.getSelectionModel().select(value);
        }
    }

    public ComboBox<String> getHighlightSelector() {
        return highlightSelector;
    }

    @FXML
    private void backToDashboardClicked(ActionEvent event) {
        if (onBackToDashboard != null) onBackToDashboard.run();
    }

    private void handleTheme() {
        themeSelector.getItems().setAll(
                Theme.LIGHT.toString(),
                Theme.DARK.toString(),
                Theme.YELLOW_BLUE.toString()
        );
        var prefs = java.util.prefs.Preferences.userNodeForPackage(getClass());
        String saved = prefs.get(PREF_KEY_THEME, Theme.LIGHT.toString());
        themeSelector.getItems().setAll(Theme.LIGHT.toString(), Theme.DARK.toString(), Theme.YELLOW_BLUE.toString());
        themeSelector.getSelectionModel().select(saved);

        // apply current once UI is ready
        Platform.runLater(() -> applySelectedTheme(saved));

        // react to changes
        themeSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                applySelectedTheme(newVal);
                prefs.put(PREF_KEY_THEME, newVal);
            }
        });
    }

    private void applySelectedTheme(String themeKey) {
        if (themeKey == null) themeKey = Theme.LIGHT.toString();
        if (btnBackToDashboard == null || btnBackToDashboard.getScene() == null) return;

        var scene = btnBackToDashboard.getScene();
        var sheets = scene.getStylesheets();
        sheets.clear();

        addCss(sheets, "/ui/styles/light.css"); // base
        String themePath = switch (themeKey) {
            case "Dark" -> "/ui/styles/dark.css";
            case "Yellow-Blue" -> "/ui/styles/yellowblue.css";
            default -> "/ui/styles/light.css";
        };
        addCss(sheets, themePath);

        String themeCssClass = switch (themeKey) {
            case "Dark"        -> "dark";
            case "Yellow-Blue" -> "yellowblue";
            default            -> "light";
        };

        themedTables.forEach(tableView -> applyThemeToTable(tableView, themeCssClass));
    }

    private void addCss(List<String> sheets, String path) {
        var url = getClass().getResource(path);
        if (url == null) {
            System.err.println("CSS not found on classpath: " + path);
            return;
        }
        sheets.add(url.toExternalForm());
    }

    private void applyThemeToTable(TableView<?> table, String cssClass) {
        if (table == null) {
            return;
        }
        table.getStyleClass().removeAll("light", "dark", "yellowblue");
        table.getStyleClass().add(cssClass);
        table.refresh();
    }

    public void registerThemedTable(TableView<?> table) {
        if (table != null && !themedTables.contains(table)) {
            themedTables.add(table);
        }
    }

}
