package ui.support;

import javafx.scene.control.Alert;
import javafx.stage.Window;

public final class Dialogs {
    private Dialogs() { }

    public static void warning(String title, String message, Window owner) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        if (owner != null) a.initOwner(owner);
        a.showAndWait();
    }


    public static void error(String title, String message, Window owner) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        if (owner != null) a.initOwner(owner);
        a.showAndWait();
    }
}
