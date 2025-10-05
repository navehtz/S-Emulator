package ui.components;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.util.*;

public class VariablesTableController {

    public record VarRow(String name, Long value) {
    }

    @FXML private TableView<VarRow> table;
    @FXML private TableColumn<VarRow, String> colName;
    @FXML private TableColumn<VarRow, Number> colValue;

    private final ObservableList<VarRow> rows = FXCollections.observableArrayList();

    private Set<String> changedNow = Collections.emptySet();

    @FXML
    private void initialize() {
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().name));
        colValue.setCellValueFactory(d -> new SimpleLongProperty(d.getValue().value));
        table.setItems(rows);

        table.setRowFactory(tv -> new TableRow<VarRow>() {
            @Override protected void updateItem(VarRow item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("");
                if (empty || item == null) return;
                if (changedNow.contains(item.name())) {
                    // soft green background for changed values (adjust / CSS)
                    setStyle("-fx-background-color: rgba(72,180,97,0.25);");
                }
            }
        });
    }

    /** expects a map like {y=3, x1=0, x2=4, z1=7} */
    public void setVariables(Map<String,Long> vars) {
        rows.clear();
        // order: y, x1..xn, z1..zn (stable)
        Map<String,Long> sorted = new TreeMap<>(new VarOrder());
        sorted.putAll(vars);
        for (var e : sorted.entrySet()) rows.add(new VarRow(e.getKey(), e.getValue()));
        table.refresh();
    }

    public TableView<VarRow> getTable() { return table; }

    private static class VarOrder implements Comparator<String> {
        @Override public int compare(String a, String b) { return key(a).compareTo(key(b)); }
        private String key(String s){
            if ("y".equals(s)) return "0";
            if (s.startsWith("x")) return "1"+pad(num(s.substring(1)));
            if (s.startsWith("z")) return "2"+pad(num(s.substring(1)));
            return "9"+s;
        }
        private static int num(String t){ try { return Integer.parseInt(t); } catch(Exception e){ return 9999; } }
        private static String pad(int n){ return String.format("%05d", n); }
    }

    public void clearVariables() {
        rows.clear();
    }

    public void highlightChanged(Set<String> names) {
        this.changedNow = (names == null) ? Collections.emptySet() : new HashSet<>(names);
        table.refresh();
    }

}
