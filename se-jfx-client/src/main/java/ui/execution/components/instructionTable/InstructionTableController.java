package ui.execution.components.instructionTable;

import ui.execution.components.instructionHistoryChain.InstructionHistoryChainController;
import dto.execution.InstructionDTO;
import dto.execution.ProgramDTO;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.*;
import java.util.function.Supplier;

public class InstructionTableController {


    @FXML private TableView<InstructionDTO> table;
    @FXML private TableColumn<InstructionDTO, Number> colBp;
    @FXML private TableColumn<InstructionDTO, Number> colIdx;
    @FXML private TableColumn<InstructionDTO, String> colType;
    @FXML private TableColumn<InstructionDTO, String> colLabel;
    @FXML private TableColumn<InstructionDTO, String> colCommand;
    @FXML private TableColumn<InstructionDTO, Number> colCycles;
    @FXML private TableColumn<InstructionDTO, String> colArchitecture;

    private final ObservableList<InstructionDTO> rows = FXCollections.observableArrayList();

    private final Set<BreakpointKey> breakpoints = new HashSet<>();
    private final IntegerProperty currentExecIndex = new SimpleIntegerProperty(-1);


    @FXML
    private void initialize() {
        colIdx.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().instructionNumber()));
        colType.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().instructionTypeStr()));
        colLabel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().labelStr()));
        colCommand.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().command()));
        colCycles.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().cycleNumber()));
        colArchitecture.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().architectureStr()));
        table.setItems(rows);

        colBp.setCellValueFactory(d -> new SimpleIntegerProperty(1));
        colBp.setSortable(false);
        colBp.setReorderable(false);
        colBp.setResizable(false);
        colBp.setCellFactory(column -> new TableColumnCell(breakpoints));

        table.setRowFactory(tableView -> new TableRow<InstructionDTO>() {
            @Override
            protected void updateItem(InstructionDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    return;
                }

                boolean isDebugRow = getIndex() == currentExecIndex.get();
                setStyle(isDebugRow ? "-fx-background-color: #fff3cd;" : "");
            }
        });

        currentExecIndex.addListener((obs, oldVal, newVal) -> table.refresh() );
    }

    public void markCurrentInstruction(int zeroBasedIndex) {
        currentExecIndex.set(zeroBasedIndex);
        if (zeroBasedIndex >= 0 && zeroBasedIndex < rows.size()) {
            table.getSelectionModel().clearAndSelect(zeroBasedIndex);
            table.scrollTo(zeroBasedIndex);
        } else {
            table.getSelectionModel().clearSelection();
        }
    }

    public void setItems(List<InstructionDTO> items) {
        rows.setAll(items);

        var valid = items.stream().map(InstructionDTO::instructionNumber).collect(java.util.stream.Collectors.toSet());
        breakpoints.removeIf(k -> !valid.contains(k.instructionNumber));
        table.refresh(); // repaint BP column states
    }

    public TableView<InstructionDTO> getTable() { return table; }
    public InstructionDTO getSelected() { return table.getSelectionModel().getSelectedItem(); }

    public String commandTextOf(InstructionDTO item) {
        if (item == null) return "";
        var ov = colCommand.getCellObservableValue(item);
        return ov == null || ov.getValue() == null ? "" : String.valueOf(ov.getValue());
    }

    public List<Boolean> getBreakpoints() {
        List<Boolean> result = new ArrayList<>();
        for (InstructionDTO instruction : rows) {
            BreakpointKey key = new BreakpointKey(instruction);
            result.add(breakpoints.contains(key));
        }
        return result;
    }


    public void bindHistoryTable(Supplier<ProgramDTO> currentProgramSupplier, InstructionHistoryChainController historyInstrTableController) {
        getTable().getSelectionModel()
                .selectedIndexProperty()
                .addListener((obs, oldIdx, newIdx) -> {
                    int i = (newIdx == null) ? -1 : newIdx.intValue();
                    ProgramDTO currentProgramDTO = currentProgramSupplier.get();

                    if (currentProgramDTO != null &&
                            i >= 0 &&
                            i < currentProgramDTO.expandedProgram().size()) {
                        List<InstructionDTO> chain = currentProgramDTO.expandedProgram().get(i);
                        historyInstrTableController.setItems(chain);
                    } else {
                        historyInstrTableController.setItems(Collections.emptyList());
                    }
                });
    }

    private static class BreakpointKey {
        private final int instructionNumber;
        BreakpointKey(InstructionDTO instruction) {
            this.instructionNumber = instruction.instructionNumber();
        }
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BreakpointKey k)) return false;
            return instructionNumber == k.instructionNumber;
        }
        @Override public int hashCode() { return Integer.hashCode(instructionNumber); }
    }

    private class TableColumnCell extends TableCell<InstructionDTO, Number> {
        private final Circle circle = new Circle(8, Color.TRANSPARENT);
        private final StackPane content = new StackPane(circle);
        private final Set<BreakpointKey> breakpointSet;

        public TableColumnCell(Set<BreakpointKey> breakpointSet) {
            this.breakpointSet = breakpointSet;

            content.setOnMouseClicked(this::handleClick);
            content.setOnMouseEntered(e -> handleHover(true));
            content.setOnMouseExited(e -> handleHover(false));
        }

        private void handleClick(MouseEvent event) {
            InstructionDTO instruction = getTableRow() != null ? getTableRow().getItem() : null;
            if (instruction == null) return;

            BreakpointKey key = new BreakpointKey(instruction);
            if (!breakpointSet.remove(key)) {
                breakpointSet.add(key);
            }
            getTableView().refresh();
        }

        private void handleHover(boolean isHovering) {
            InstructionDTO instruction = getTableRow() != null ? getTableRow().getItem() : null;
            if (instruction == null) return;

            boolean hasBreakpoint = breakpointSet.contains(new BreakpointKey(instruction));
            if (!hasBreakpoint) {
                circle.setFill(isHovering ? Color.rgb(255, 4, 0, 0.65) : Color.TRANSPARENT);
            }
        }

        @Override
        protected void updateItem(Number item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);               // detach for empty rows
                return;
            }

            // Always re-attach the content on non-empty updates
            setGraphic(content);

            // Repaint the circle according to the current row’s BP state
            InstructionDTO instruction = getTableRow() != null ? getTableRow().getItem() : null;
            if (instruction == null) {
                circle.setFill(Color.TRANSPARENT);
                return;
            }
            boolean hasBreakpoint = breakpointSet.contains(new BreakpointKey(instruction));
            circle.setFill(hasBreakpoint ? Color.rgb(255, 4, 0, 0.65) : Color.TRANSPARENT);
        }
    }

    public void selectRow(int index) {
        if (index < 0) return;
        table.getSelectionModel().clearAndSelect(index);
        table.scrollTo(index);
    }
}
