package ui.components;

import javafx.beans.property.*;

public class InputRow {
    private final StringProperty name = new SimpleStringProperty();
    private final LongProperty value = new SimpleLongProperty();

    public InputRow(String name, int value) {
        this.name.set(name);
        this.value.set(value);
    }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    public long getValue() { return value.get(); }
    public LongProperty valueProperty() { return value; }
}
