// src/main/java/ui/MainController.java
package ui;

import dto.InstructionDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import engine.Engine;
import engine.EngineImpl;
import exceptions.EngineLoadException;
import javafx.beans.property.*;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TableRow;

import ui.components.*;
import ui.run.RunCoordinator;
import ui.run.RunResultPresenter;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainController {

    @FXML private InstructionTableController mainInstrTableController;
    @FXML private InstructionTableController historyInstrTableController;
    @FXML private VariablesTableController   varsPaneController;
    @FXML private DynamicInputsController    inputsPaneController;
    @FXML private RunHistoryTableController  runsPaneController;

     @FXML private Label programNameLabel;
     @FXML private ComboBox<String> contextSelector;
     @FXML private ComboBox<Integer> degreeSelector;
     @FXML private ComboBox<String> highlightSelector;
     @FXML private Button btnRun;
     @FXML private Button btnDebug;
     @FXML private Button btnStop;
     @FXML private Button btnResume;
     @FXML private Button btnStepOver;
     @FXML private Label cyclesLabel;

    private final Engine engine = new EngineImpl();
    private final ObjectProperty<ProgramDTO> currentProgramDTO = new SimpleObjectProperty<>() {};
    private RunCoordinator runCoordinator;
    private final BooleanProperty isRunInProgress = new SimpleBooleanProperty(false);

    public ReadOnlyObjectProperty<ProgramDTO> currentProgramProperty() { return currentProgramDTO; }
    public BooleanProperty isRunInProgressProperty() { return isRunInProgress; }
    public ProgramDTO getCurrentProgram() { return currentProgramDTO.get(); }
    //public void setCurrentProgram(ProgramDTO p) { currentProgramDTO.set(p); }

    private int numOfRuns = 0;
    private final List<ProgramExecutorDTO> historyOfRuns = new ArrayList<>();

    private static final PseudoClass HIGHLIGHTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("var-highlight");

    @FXML
    private void initialize() {
        initUiWiring();
        setupMainTableHighlighting();
    }

    private void initUiWiring() {
        initDegreeSelectorHandler();
        initRunButtonHandler();
        mainInstrTableController.bindHistoryTable(currentProgramDTO::get, historyInstrTableController);
    }

    private void initRunButtonHandler() {
        btnRun.setOnAction(e -> handleRunClick());
        initRunButtonDisableBinding();
    }

    private void handleRunClick() {
        if (runCoordinator == null) {
            Window owner = getOwnerWindowOrNull();
            if (owner == null) {
                new Alert(Alert.AlertType.WARNING, "Window not ready yet. Try again.").show();
                return;
            }
            runCoordinator = new RunCoordinator(
                    engine,
                    owner,
                    this::getSelectedDegree,   // your supplier for the selected degree
                    new UiRunPresenter()
            );
        }

        runCoordinator.executeForRun(getCurrentProgram());
    }

    private Window getOwnerWindowOrNull() {
        return (btnRun.getScene() != null) ? btnRun.getScene().getWindow() : null;
    }

    private void initDegreeSelectorHandler() {
        degreeSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ProgramDTO expanded = engine.getExpandedProgramToDisplay(newVal);
                updateCurrentProgramAndMainInstrTable(expanded); // updates currentProgram + mainInstr table
                clearExecutionData();
                populateHighlightSelectorFromCurrentProgram();
            }
        });
    }

    private void populateHighlightSelectorFromCurrentProgram() {
        ProgramDTO currentProgram = getCurrentProgram();
        if (currentProgram == null) {
            highlightSelector.getItems().clear();
        } else {
            highlightSelector.getItems().setAll(currentProgram.allVariables());
        }
    }

    private void initRunButtonDisableBinding() {
        btnRun.disableProperty().bind(
                currentProgramProperty().isNull()
                        .or(isRunInProgressProperty())
        );
    }

    // ===== Handlers used in MainView.fxml =====
    @FXML private void onLoadXml(ActionEvent e) throws EngineLoadException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("xml files", "*.xml"),
                new FileChooser.ExtensionFilter("all files", "*.*")
        );

        Window window = contextSelector.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile == null || !selectedFile.getName().endsWith(".xml") ) {
            showAlert(Alert.AlertType.WARNING, "No Program Loaded", "Please load a program file before running.");
            return;
        }

        programNameLabel.setText(selectedFile.getAbsolutePath());
        engine.loadProgram(Path.of(selectedFile.getAbsolutePath()));
        clearExecutionData();
        runsPaneController.clearHistory();

        ProgramDTO baseProgram = engine.getProgramToDisplay();
        updateCurrentProgramAndMainInstrTable(baseProgram);

        int maxDegree = engine.getMaxDegree();
        degreeSelector.getItems().setAll(
                java.util.stream.IntStream.rangeClosed(0, maxDegree).boxed().toList()
        );
        degreeSelector.getSelectionModel().selectFirst();
        highlightSelector.getItems().setAll(baseProgram.allVariables());
        //activateButtons();
    }

    @FXML private void onRun(ActionEvent e) {
        if (getCurrentProgram() == null || isRunInProgress.get()) return;
        runCoordinator.executeForRun(getCurrentProgram());
    }
    @FXML private void onDebug(ActionEvent e)      {  }
    @FXML private void onStop(ActionEvent e)       {  }
    @FXML private void onResume(ActionEvent e)     {  }
    @FXML private void onStepOver(ActionEvent e)   {  }

    private int getSelectedDegree() {
        if (degreeSelector == null || degreeSelector.getValue() == null) return 0;
        return degreeSelector.getValue();
    }

    private void updateCurrentProgramAndMainInstrTable(ProgramDTO dto) {
        this.currentProgramDTO.set(dto);
        mainInstrTableController.setItems(dto.instructions().programInstructionsDTOList());
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearExecutionData() {
        inputsPaneController.clearInputs();
        varsPaneController.clearVariables();
    }


    @FXML private void updateInputsPane(ProgramExecutorDTO programExecutorDTO) {
        List<Long> inputsValues = programExecutorDTO.inputsValuesOfUser();
        inputsPaneController.setInputsValuesOfUser(inputsValues);
    }

    private final class UiRunPresenter implements RunResultPresenter {
        @Override public void onRunStarted() {
            isRunInProgress.set(true);
            //  show a busy indicator
        }
        @Override public void onRunSucceeded(ProgramExecutorDTO programExecutorDTO) {
            try {
                // 1) Variables: y, then Xi asc, then Zi asc + cycles
                Map<String, Long> sortedVariables = new LinkedHashMap<>();
                sortedVariables.put("y", programExecutorDTO.result());
                sortedVariables.putAll((Map<? extends String, ? extends Long>) programExecutorDTO.variablesToValuesSorted().entrySet().stream()
                                // Filter out variables that start with "x" or "X"
                                .filter(entry -> !entry.getKey().toLowerCase().startsWith("x"))
                                // Collect the remaining entries into a sorted LinkedHashMap
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (oldValue, newValue) -> oldValue,
                                        LinkedHashMap::new
                                )));

                        varsPaneController.setVariables(sortedVariables);
                cyclesLabel.setText(String.valueOf(programExecutorDTO.totalCycles()));

                updateInputsPane(programExecutorDTO);


                // 3) History aggregation (program name + degree + inputs + outputs + cycles)

                appendRunToHistory(++numOfRuns,
                        programExecutorDTO.degree(),
                        programExecutorDTO.inputsValuesOfUser(),
                        programExecutorDTO.result(),
                        programExecutorDTO.totalCycles() );

                historyOfRuns.add(programExecutorDTO);

            } finally {
                isRunInProgress.set(false);
            }
        }
        @Override public void onRunFailed(String message) {
            isRunInProgress.set(false);
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Run failed");
            alert.setHeaderText("Run failed");
            alert.setContentText(message != null ? message : "Unknown error");
            alert.showAndWait();
        }
    }

    private void appendRunToHistory(int runNumber, int degree, List<Long> inputs, long result, int cycles) {
        String inputsFormatted = inputs.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        RunHistoryTableController.RunRow runRow = new RunHistoryTableController.RunRow(
                runNumber,
                degree,
                inputsFormatted,
                result,
                cycles
        );

        // Append the new RunRow to the table
        runsPaneController.appendRow(runRow);
    }

    private void setupMainTableHighlighting() {
        var tv = mainInstrTableController.getTable();

        tv.setRowFactory(_tv -> {
            TableRow<InstructionDTO> row = new TableRow<>();

            // Re-apply when row item changes, row selection changes, or selector changes
            ChangeListener<Object> applier = (obs, o, n) -> applyVarHighlight(row);
            row.itemProperty().addListener(applier);
            row.selectedProperty().addListener(applier);
            highlightSelector.valueProperty().addListener(applier);

            return row;
        });

        // When items list is replaced (degree/load), refresh to re-evaluate
        tv.itemsProperty().addListener((obs, o, n) -> tv.refresh());
    }

    private void applyVarHighlight(TableRow<InstructionDTO> row) {
        InstructionDTO instructionFromRow = row.getItem();
        String chosenVariable = highlightSelector.getValue();

        boolean isRowChosen = chosenVariable != null && !chosenVariable.isBlank();
        boolean isMatchedRows = isRowChosen && instructionFromRow != null
                && containsVariable(mainInstrTableController.commandTextOf(instructionFromRow), chosenVariable);

        boolean apply = isMatchedRows && !row.isSelected();

        applyPseudoClassHighlight(apply, row);
    }

    private boolean containsVariable(String command, String variable) {
        return command.contains(variable);
    }

    private void applyPseudoClassHighlight(boolean apply, TableRow<InstructionDTO> row) {
        row.pseudoClassStateChanged(HIGHLIGHTED_PSEUDO_CLASS, apply);
    }

}


