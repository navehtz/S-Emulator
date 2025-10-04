// src/main/java/ui/MainController.java
package ui;

import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import engine.Engine;
import engine.EngineImpl;
import exceptions.EngineLoadException;
import javafx.animation.PauseTransition;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.*;

import javafx.util.Duration;
import ui.components.*;
import ui.run.RunOrchestrator;
import ui.run.RunUiPresenter;
import ui.support.RunsHistoryManager;
import ui.support.VariablesPaneUpdater;
import ui.support.Dialogs;
import ui.behavior.HighlightingBehavior;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                runUiPresenter,
                this::selectedOperationKey
        );
    }

    private void initUiWiring() {
        initDegreeSelectorHandler();
        initContextSelectorHandler();
        initRunButtonDisableBinding();
        mainInstrTableController.bindHistoryTable(currentProgramDTO::get, historyInstrTableController);
    }

    private Window getOwnerWindowOrNull() {
        return (btnRun.getScene() != null) ? btnRun.getScene().getWindow() : null;
    }

    private void initDegreeSelectorHandler() {
        degreeSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String selectedContext = contextSelector.getValue() != null
                    ? contextSelector.getValue()
                    : engine.getProgramToDisplay().programName();
            if (selectedContext == null) return;
            String selectedFunction = engine.getAllUserStringToFunctionName().get(selectedContext);
            ProgramDTO expanded = engine.getExpandedProgramDTO(selectedFunction, newVal);
            updateCurrentProgramAndMainInstrTable(expanded); // updates currentProgram + mainInstr table
            clearExecutionData();
            populateHighlightSelectorFromCurrentProgram();
        });
    }

    private void initContextSelectorHandler() {
        contextSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;

            int currentDegree = degreeSelector.getValue() != null ? degreeSelector.getValue() : 0;
            String selectedFunction = engine.getAllUserStringToFunctionName().get(newVal);

            int maxDegreeForContext;
            try {
                maxDegreeForContext = engine.getMaxDegree(selectedFunction);
            } catch (Exception e) {
                degreeSelector.getItems().setAll(0);
                degreeSelector.getSelectionModel().select(Integer.valueOf(0));
                return;
            }

            var degrees = java.util.stream.IntStream.rangeClosed(0, maxDegreeForContext)
                    .boxed().toList();
            degreeSelector.getItems().setAll(degrees);

            int selectedDegree = (currentDegree <= maxDegreeForContext) ? currentDegree : 0;
            degreeSelector.getSelectionModel().select(Integer.valueOf(selectedDegree));

            ProgramDTO expanded = engine.getExpandedProgramDTO(selectedFunction, degreeSelector.getValue() != null ? degreeSelector.getValue() : 0);
            updateCurrentProgramAndMainInstrTable(expanded); // updates currentProgram + mainInstr table
            clearExecutionData();
            populateHighlightSelectorFromCurrentProgram();
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
        fileChooser.setTitle("Open Program XML");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("xml files", "*.xml"),
                new FileChooser.ExtensionFilter("all files", "*.*")
        );

        final Window window = contextSelector.getScene().getWindow();
        final File selectedFile = fileChooser.showOpenDialog(window);

        if (selectedFile == null) {
            Dialogs.warning("No file selected", "Please choose an XML file.", window);
            return;
        }

        final String name = selectedFile.getName().toLowerCase();

        if (!(name.endsWith(".xml"))) {
            Dialogs.warning("Invalid File", "Please choose an XML file.", window);
            return;
        }

        programNameLabel.setText(selectedFile.getAbsolutePath());

        final Path programPath = Path.of(selectedFile.getAbsolutePath());

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception{
                engine.loadProgram(programPath);
                return null;
            }
        };

        task.setOnSucceeded(ev -> {

            ProgramDTO baseProgram = engine.getProgramToDisplay();
            updateCurrentProgramAndMainInstrTable(baseProgram);

            try {
                List<String> contextNames = new ArrayList<>();
                contextNames.add(baseProgram.programName());
                contextNames.addAll(engine.getAllUserStringToFunctionName().keySet());
                contextSelector.getItems().setAll(contextNames);
                //contextSelector.getItems().setAll(engine.getAllFunctionsNames());
                contextSelector.getSelectionModel().selectFirst();

                degreeSelector.getItems().setAll(
                        java.util.stream.IntStream.rangeClosed(0, engine.getMaxDegree()).boxed().toList()
                );
                degreeSelector.getSelectionModel().selectFirst();
            } catch (EngineLoadException ex) {
                throw new RuntimeException(ex);
            }

            highlightSelector.getItems().setAll(baseProgram.allVariables());

            clearExecutionData();
            runsHistoryManager.clearHistory();
        });

        task.setOnFailed(ev -> {
            Throwable err = rootCause(task.getException());
            err.printStackTrace(); // useful in console
            Dialogs.error("Load failed", String.valueOf(err.getMessage()), getOwnerWindowOrNull());
        });

        showLoadingPopup(task, window);

        Thread loadThread = new Thread(task, "load-xml-task");
        loadThread.setDaemon(true);
        loadThread.start();
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

    private void showLoadingPopup(Task<?> task, Window owner) {
        // Progress bar bound to the task
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(250);
        progressBar.progressProperty().bind(task.progressProperty());

        Label label = new Label("Loading program, please wait...");

        VBox root = new VBox(10, label, progressBar);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Stage dialog = new Stage(StageStyle.UTILITY);
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(new Scene(root));
        dialog.setTitle("Loading…");

        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e -> closeLater(dialog));
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, e -> closeLater(dialog));
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, e -> closeLater(dialog));

        dialog.show();
    }

    private void closeLater(Stage dialog) {
        PauseTransition pause = new PauseTransition(Duration.seconds(1.1));
        pause.setOnFinished(_e -> dialog.close());
        pause.play();
    }

    private static Throwable rootCause(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null) c = c.getCause();
        return c;
    }

    private String selectedOperationKey() {
        String selectedOperationString = contextSelector.getValue();
        if (selectedOperationString == null || selectedOperationString.isBlank()) {
            return engine.getProgramToDisplay().programName(); // main program fallback
        }

        return engine.getAllUserStringToFunctionName().getOrDefault(selectedOperationString, selectedOperationString);
    }

}


