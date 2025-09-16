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
import ui.run.RunOrchestrator;
import ui.run.RunResultPresenter;
import ui.run.RunUiPresenter;
import ui.support.RunsHistoryManager;
import ui.support.VariablesPaneUpdater;
import ui.support.Dialogs;
import ui.behavior.HighlightingBehavior;

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
    private final BooleanProperty isRunInProgress = new SimpleBooleanProperty(false);

    private RunOrchestrator runOrchestrator;
    private RunsHistoryManager runsHistoryManager;

    public ReadOnlyObjectProperty<ProgramDTO> currentProgramProperty() { return currentProgramDTO; }
    public BooleanProperty isRunInProgressProperty() { return isRunInProgress; }
    public ProgramDTO getCurrentProgram() { return currentProgramDTO.get(); }


    @FXML
    private void initialize() {
        initCollaborators();
        initUiWiring();
        new HighlightingBehavior("var-highlight").wire(
                mainInstrTableController.getTable(),
                highlightSelector,
                mainInstrTableController::commandTextOf);
    }

    private void initCollaborators() {
        VariablesPaneUpdater variablesPaneUpdater = new VariablesPaneUpdater(varsPaneController, cyclesLabel);
        runsHistoryManager = new RunsHistoryManager(runsPaneController);

        RunUiPresenter runUiPresenter = new RunUiPresenter(
                isRunInProgress,
                variablesPaneUpdater,
                runsHistoryManager,
                this::updateInputsPane);

        this.runOrchestrator = new RunOrchestrator(
                engine,
                this::getOwnerWindowOrNull,
                this::getSelectedDegree,
                isRunInProgress,
                runUiPresenter
        );
    }

    private void initUiWiring() {
        initDegreeSelectorHandler();
        initRunButtonDisableBinding();
        mainInstrTableController.bindHistoryTable(currentProgramDTO::get, historyInstrTableController);
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
            Dialogs.warning("No Program Loaded", "Please load a program file before running.", window);
            return;
        }

        programNameLabel.setText(selectedFile.getAbsolutePath());
        engine.loadProgram(Path.of(selectedFile.getAbsolutePath()));
        clearExecutionData();
        runsHistoryManager.clearHistory();

        ProgramDTO baseProgram = engine.getProgramToDisplay();
        updateCurrentProgramAndMainInstrTable(baseProgram);

        int maxDegree = engine.getMaxDegree();
        degreeSelector.getItems().setAll(
                java.util.stream.IntStream.rangeClosed(0, maxDegree).boxed().toList()
        );
        degreeSelector.getSelectionModel().selectFirst();
        highlightSelector.getItems().setAll(baseProgram.allVariables());
    }

    @FXML private void onRun(ActionEvent e) {
        runOrchestrator.run(getCurrentProgram());
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

    private void clearExecutionData() {
        inputsPaneController.clearInputs();
        varsPaneController.clearVariables();
    }


    @FXML private void updateInputsPane(ProgramExecutorDTO programExecutorDTO) {
        List<Long> inputsValues = programExecutorDTO.inputsValuesOfUser();
        inputsPaneController.setInputsValuesOfUser(inputsValues);
    }
}


