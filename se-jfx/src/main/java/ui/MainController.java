// src/main/java/ui/MainController.java
package ui;

import dto.InstructionDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import engine.Engine;
import engine.EngineImpl;
import exceptions.EngineLoadException;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import ui.components.*;
import ui.run.RunCoordinator;
import ui.run.RunResultPresenter;
import variable.Variable;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MainController {

    // These are injected because of <fx:include fx:id="...">
    @FXML private InstructionTableController mainInstrTableController;
    @FXML private InstructionTableController historyInstrTableController;
    @FXML private VariablesTableController   varsPaneController;
    @FXML private DynamicInputsController    inputsPaneController;
    @FXML private RunHistoryTableController  runsPaneController;

    // Top-bar buttons/controls may also be referenced by fx:id in FXML (optional)
     @FXML private Label programNameLabel;
     @FXML private ComboBox<String> contextSelector;
     @FXML private ComboBox<Integer> degreeSelector;
     @FXML private ComboBox<String> highlightSelector;
     @FXML private Button btnRun;
     @FXML private Button btnStop;
     @FXML private Button btnResume;
     @FXML private Button btnStepOver;
     @FXML private Button btnDebug;
     @FXML private Label cyclesLabel;


    private final Engine engine = new EngineImpl();
    private final ObjectProperty<ProgramDTO> currentProgramDTO = new SimpleObjectProperty<>() {};
    public ReadOnlyObjectProperty<ProgramDTO> currentProgramProperty() { return currentProgramDTO; }
    private final BooleanProperty runInProgress = new SimpleBooleanProperty(false);
    public BooleanProperty runInProgressProperty() { return runInProgress; }
    private RunCoordinator runCoordinator;

    public ProgramDTO getCurrentProgram() { return currentProgramDTO.get(); }
    public void setCurrentProgram(ProgramDTO p) { currentProgramDTO.set(p); }
    private int numOfRuns = 0;
    private List<ProgramExecutorDTO> historyOfRuns = new ArrayList<>();

    @FXML
    private void initialize() {
        mainInstrTableController.getTable().getSelectionModel()
                .selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
                    int i = (newIdx == null) ? -1 : newIdx.intValue();
                    if (i >= 0 && i < currentProgramDTO.get().expandedProgram().size()) {

                        List<InstructionDTO> chain = currentProgramDTO.get().expandedProgram().get(i);
                        historyInstrTableController.setItems(chain);
                    } else {
                        historyInstrTableController.setItems(java.util.Collections.emptyList());
                    }
                });

        degreeSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ProgramDTO expanded = engine.getExpandedProgramToDisplay(newVal);
                updateCurrentProgramAndMainInstrTable(expanded); // updates currentProgram + mainInstr table
                clearExecutionData();
            }
        });

        btnRun.disableProperty().bind(
                currentProgramProperty().isNull()
                        .or(runInProgressProperty())
        );

        btnRun.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) return;
            Window owner = newScene.getWindow();
            runCoordinator = new RunCoordinator(
                    engine,
                    owner,
                    this::getSelectedDegree,
                    new UiRunPresenter()
            );
        });
    }

    private int getSelectedDegree() {
        if (degreeSelector == null || degreeSelector.getValue() == null) return 0;
        return degreeSelector.getValue();
    }

    private void updateCurrentProgramAndMainInstrTable(ProgramDTO dto) {
        this.currentProgramDTO.set(dto);
        mainInstrTableController.setItems(dto.instructions().programInstructionsDtoList());
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
        //activateButtons();
    }

    @FXML private void onRun(ActionEvent e) {
        if (getCurrentProgram() == null || runInProgress.get()) return;
        runCoordinator.executeForRun(getCurrentProgram());

    }
    @FXML private void onDebug(ActionEvent e)      {  }
    @FXML private void onStop(ActionEvent e)       {  }
    @FXML private void onResume(ActionEvent e)     {  }
    @FXML private void onStepOver(ActionEvent e)   {  }

    // If your toolbar has other onAction handlers, add them here too.

  /*  private void activateButtons() {
        btnRun.setDisable(false);
        btnDebug.setDisable(false);
        btnStop.setDisable(true);
        btnResume.setDisable(true);
        btnStepOver.setDisable(false);
    }*/

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
            runInProgress.set(true);
            // Optional: show a tiny busy indicator
        }
        @Override public void onRunSucceeded(ProgramExecutorDTO programExecutorDTO) {
            try {
                // 1) Variables: y, then Xi asc, then Zi asc + cycles
                Map<String, Long> sorted = new LinkedHashMap<>();
                sorted.put("y", programExecutorDTO.result());
                sorted.putAll((Map<? extends String, ? extends Long>) programExecutorDTO.variablesToValuesSorted().entrySet().stream()
                                // Filter out variables that start with "x" or "X"
                                .filter(entry -> !entry.getKey().toLowerCase().startsWith("x"))
                                // Collect the remaining entries into a sorted LinkedHashMap
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        Map.Entry::getValue,
                                        (oldValue, newValue) -> oldValue,
                                        LinkedHashMap::new
                                )));

                        varsPaneController.setVariables(sorted);
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
                runInProgress.set(false);
            }
        }
        @Override public void onRunFailed(String message) {
            runInProgress.set(false);
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


    // ===== Ordering helper (kept local for cohesion; replace with your existing util if you prefer) =====
    private static final Pattern XI = Pattern.compile("x(\\d+)");
    private static final Pattern ZI = Pattern.compile("z(\\d+)");


}


