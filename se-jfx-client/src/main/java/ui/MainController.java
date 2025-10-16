// src/main/java/ui/MainController.java
package ui;

import dto.DebugDTO;
import dto.InstructionsDTO;
import dto.ProgramDTO;
import dto.ProgramExecutorDTO;
import engine.Engine;
import engine.EngineImpl;
import exceptions.EngineLoadException;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.*;

import javafx.util.Duration;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ui.components.*;
import ui.debug.DebugOrchestrator;
import ui.debug.DebugUiPresenter;
import ui.run.RunOrchestrator;
import ui.run.RunUiPresenter;
import ui.support.RunsHistoryManager;
import ui.support.VariablesPaneUpdater;
import ui.support.Dialogs;
import ui.behavior.HighlightingBehavior;
import ui.themes.Theme;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class MainController {

    @FXML private VBox rightPane;
    @FXML private InstructionTableController mainInstrTableController;
    @FXML private InstructionHistoryChainController historyInstrTableController;
    @FXML private VariablesTableController   varsPaneController;
    @FXML private DynamicInputsController    inputsPaneController;
    @FXML private RunHistoryTableController  runsPaneController;
    @FXML private SummaryLineController      summaryLineController;

    @FXML private Label programNameLabel;
    @FXML private ComboBox<String> contextSelector;
    @FXML private ComboBox<Integer> degreeSelector;
    @FXML private ComboBox<String> highlightSelector;
    @FXML private Button btnLoadFile;
    @FXML private Button btnRun;
    @FXML private Button btnDebug;
    @FXML private Button btnStop;
    @FXML private Button btnResume;
    @FXML private Button btnStepOver;
    @FXML private Button btnStepBack;
    @FXML private Label cyclesLabel;
    @FXML private ScrollPane rootScroll;
    @FXML private BorderPane rootContent;
    @FXML private CheckBox checkBoxAnimations;
    @FXML private ComboBox<String> themeSelector;

    private final Engine engine = new EngineImpl();
    private final ObjectProperty<ProgramDTO> currentProgramDTO = new SimpleObjectProperty<>() {};
    private final BooleanProperty isRunInProgress = new SimpleBooleanProperty(false);
    private final BooleanProperty isDebugInProgress = new SimpleBooleanProperty(false);

    private RunOrchestrator runOrchestrator;
    private RunsHistoryManager runsHistoryManager;
    private DebugOrchestrator debugOrchestrator;
    private final Map<String, Long> lastVarsSnapshot = new HashMap<>();

    private SequentialTransition pulseAnimation;
    private SequentialTransition introAnimation;
    private ParallelTransition runDebugPulse;
    private BooleanProperty animationsEnabled = new SimpleBooleanProperty(true);
    private static final String PREF_KEY_THEME = "app.theme";

    public ReadOnlyObjectProperty<ProgramDTO> currentProgramProperty() { return currentProgramDTO; }
    public BooleanProperty isRunInProgressProperty() { return isRunInProgress; }
    public BooleanProperty isDebugInProgressProperty() { return isDebugInProgress; }
    public ProgramDTO getCurrentProgram() { return currentProgramDTO.get(); }


    @FXML
    private void initialize() {
        initCollaborators();
        initUiWiring();
        new HighlightingBehavior().wire(
                mainInstrTableController.getTable(),
                highlightSelector,
                mainInstrTableController::commandTextOf
        );

        rootScroll.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            double designWidth = 1100.0;
            double designHeight = 600.0;
            rootContent.setPrefWidth(Math.max(designWidth, newVal.getWidth()));
            rootContent.setPrefHeight(Math.max(designHeight, newVal.getHeight()));
        });

        rightPane.setMinWidth(400);
        rightPane.setPrefWidth(400);
        rightPane.setMaxWidth(500);

        animationsEnabled.bind(checkBoxAnimations.selectedProperty());
        animationsEnabled.addListener((obs, was, isNow) -> {
            if (isNow && !(isRunInProgress.get() || isDebugInProgress.get())) {
                animateLoadFileButton();
            }
            else {
                stopAllAnimations();
            }
        });

        Platform.runLater(() -> {
           if (animationsEnabled.get()) {
               animateLoadFileButton();
           }
        });

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

    private void initCollaborators() {
        VariablesPaneUpdater variablesPaneUpdater = new VariablesPaneUpdater(varsPaneController, cyclesLabel);
        runsHistoryManager = new RunsHistoryManager(runsPaneController);
        runsHistoryManager.setCurrentProgramKeySupplier(this::selectedOperationKey);
        summaryLineController.setProperty(currentProgramDTO);
        summaryLineController.initializeBindings();
        runsPaneController.setOnShowStatus(runsHistoryManager::showStatusPopup);
        runsPaneController.setOnRerun(row -> onRerunRow(row));


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

        DebugUiPresenter debugPresenter = new DebugUiPresenter(
                isDebugInProgress,
                variablesPaneUpdater,
                runsHistoryManager,
                this::updateInputsPane,
                this::applySnapshot,
                this::enterDebugMode
        );

        this.debugOrchestrator = new DebugOrchestrator(
                engine,
                this::getOwnerWindowOrNull,
                this::getSelectedDegree,
                isDebugInProgress,
                debugPresenter,
                this::selectedOperationKey
        );
    }

    private void initUiWiring() {
        initDegreeSelectorHandler();
        initContextSelectorHandler();
        initRunAndDebugButtonsDisableBinding();

        btnStop.disableProperty().bind(isDebugInProgress.not());
        btnResume.disableProperty().bind(isDebugInProgress.not());
        btnStepOver.disableProperty().bind(isDebugInProgress.not());
        btnStepBack.disableProperty().bind(isDebugInProgress.not());

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
            if (isDebugInProgress.get()) {
                isDebugInProgress.set(false);
            }
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

            runsHistoryManager.onContextChanged(selectedFunction);

            if (isDebugInProgress.get()) {
                isDebugInProgress.set(false);
            }

//            //TODO: Change to real history of function
//            runsHistoryManager.clearHistory();
        });
    }

    private void populateHighlightSelectorFromCurrentProgram() {
        ProgramDTO currentProgram = getCurrentProgram();
        if (currentProgram == null) {
            highlightSelector.getItems().clear();
        } else {
            highlightSelector.getItems().setAll(currentProgram.allVariables());
            highlightSelector.getItems().addAll(currentProgram.labelsStr());
        }
    }

    private void initRunAndDebugButtonsDisableBinding() {
        btnRun.disableProperty().bind(
                currentProgramProperty().isNull()
                        .or(isRunInProgressProperty())
        );

        btnDebug.disableProperty().bind(
                currentProgramProperty().isNull()
                        .or(isDebugInProgressProperty())
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
//                engine.loadProgram(programPath);
//                return null;
                OkHttpClient client = new OkHttpClient();
                String url = "http://localhost:8080/load-xml";
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Call call = client.newCall(request);
                Response response = call.execute();
                return null;
            }
        };

        task.setOnSucceeded(ev -> {

            ProgramDTO baseProgram = engine.getProgramToDisplay();
            updateCurrentProgramAndMainInstrTable(baseProgram);

            if (pulseAnimation != null) pulseAnimation.stop(); // Stop the pulsing
            btnLoadFile.setScaleX(1);
            btnLoadFile.setScaleY(1);
            btnLoadFile.setEffect(null); // Remove the glow

            try {
                List<String> contextNames = new ArrayList<>(engine.getAllUserStringToFunctionName().keySet());
                contextSelector.getItems().setAll(contextNames);
                //contextSelector.getItems().setAll(engine.getAllFunctionsNames());
                contextSelector.getSelectionModel().select(baseProgram.programName());

                degreeSelector.getItems().setAll(
                        java.util.stream.IntStream.rangeClosed(0, engine.getMaxDegree()).boxed().toList()
                );
                degreeSelector.getSelectionModel().selectFirst();
            } catch (EngineLoadException ex) {
                throw new RuntimeException(ex);
            }

            highlightSelector.getItems().setAll(baseProgram.allVariables());
            highlightSelector.getItems().addAll(baseProgram.labelsStr());

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
        btnRun.setEffect(null);
        btnDebug.setEffect(null);
    }
    @FXML private void onDebug(ActionEvent e)      {
        debugOrchestrator.debug();
        btnRun.setEffect(null);
        btnDebug.setEffect(null);
    }
    @FXML private void onStop(ActionEvent e)       {
        try {
            engine.stopDebugPress();
        } finally {
            leaveDebugMode();
        }
    }

    @FXML private void onResume(ActionEvent e)     {
        try {
            var breakpoints = mainInstrTableController.getBreakpoints();
            var d = engine.getProgramAfterResume(breakpoints);
            applySnapshot(d);
        } catch (InterruptedException ex) {
            // user cancelled, ignore
        } catch (Exception ex) {
            Dialogs.error("Resume failed", ex.getMessage(), getOwnerWindowOrNull());
            leaveDebugMode();
        }
    }

    @FXML private void onStepOver(ActionEvent e)   {
        try {
            var d = engine.getProgramAfterStepOver();
            applySnapshot(d);
        } catch (Exception exception) {
            Dialogs.error("Step Over failed", exception.getMessage(), getOwnerWindowOrNull());
            leaveDebugMode();
        }
    }

    @FXML private void onStepBack(ActionEvent actionEvent) {
        try {
            var d = engine.getProgramAfterStepBack();
            applySnapshot(d);
        } catch (Exception ex) {
            Dialogs.error("Step Back failed", ex.getMessage(), getOwnerWindowOrNull());
        }
    }

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

    private void enterDebugMode() {
        isDebugInProgress.set(true);
    }

    private void leaveDebugMode() {
        isDebugInProgress.set(false);
    }

    // After each step/resume/stepBack update
    private void updateButtonsForSnapshot(dto.ProgramExecutorDTO snap, boolean hasMore, boolean hasHistoryBack) {
    }

    private void applySnapshot(DebugDTO dbgDTO) {
        mainInstrTableController.markCurrentInstruction(dbgDTO.currentInstructionNumber());

        Set<String> changedVariables = new HashSet<>();
        Map<String, Long> variablesStateNow = dbgDTO.variablesToValuesSorted();
        for (var e : variablesStateNow.entrySet()) {
            Long variableStateBefore = lastVarsSnapshot.get(e.getKey());
            if (variableStateBefore == null || !variableStateBefore.equals(e.getValue()))
                changedVariables.add(e.getKey());
        }

        // remember for next time
        lastVarsSnapshot.clear();
        lastVarsSnapshot.putAll(variablesStateNow);
        var programExecutor = toProgramExecutor(dbgDTO);
        // update right panes
        updateInputsPane(programExecutor);                          // if you prefer to only set on session start, remove this line
        new VariablesPaneUpdater(varsPaneController, cyclesLabel).update(programExecutor, changedVariables);

        boolean hasHistory = dbgDTO.currentInstructionNumber() > 0;
        updateButtonsForSnapshot(programExecutor, dbgDTO.hasMoreInstructions(), hasHistory);

        if (!dbgDTO.hasMoreInstructions()) {
            runsHistoryManager.append(programExecutor);
            leaveDebugMode();
        }
    }

    private dto.ProgramExecutorDTO toProgramExecutor(DebugDTO dbg) {
        var stub = new dto.ProgramDTO(
                dbg.programName(),
                List.of(), List.of(),
                new InstructionsDTO(List.of()),
                List.of(),
                List.of()
        );
        return new dto.ProgramExecutorDTO(
                stub,
                dbg.variablesToValuesSorted(),
                dbg.result(),
                dbg.totalCycles(),
                dbg.degree(),
                List.of() // inputs pane is already set at session start
        );
    }

    private void onRerunRow(RunHistoryTableController.RunRow row) {
        String programKey = row.programKey();

        String displayFunction = engine.getAllUserStringToFunctionName()
                .entrySet().stream()
                .filter(e -> e.getValue().equals(programKey))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(programKey);

        contextSelector.getSelectionModel().select(displayFunction);

        // Set degree to the run’s degree
        degreeSelector.getSelectionModel().select(Integer.valueOf(row.degree()));

        // Clear panes for a fresh session
        clearExecutionData();

        // Seed inputs so the next Run/Debug dialog is prefilled
        runOrchestrator.seedPrefillInputs(programKey, row.inputs());

        pulseRunAndDebugButtons();
    }

    private void animateLoadFileButton() {
        if (!animationsEnabled.get()) return;
        btnLoadFile.setOpacity(0);
        //btnLoadFile.setTranslateY(14);

        // Drop shadow (soft)
        DropShadow ds = new DropShadow(30, Color.rgb(0,0,0,0.30));
        btnLoadFile.setEffect(ds);

        // Intro: fade + slide
        FadeTransition fade = new FadeTransition(Duration.millis(420), btnLoadFile);
        fade.setFromValue(0);
        fade.setToValue(1);

        // Pulse after intro (scale up , then back)
        ScaleTransition pulseUp = new ScaleTransition(Duration.millis(200), btnLoadFile);
        pulseUp.setToX(1.04);
        pulseUp.setToY(1.04);

        ScaleTransition pulseDown = new ScaleTransition(Duration.millis(200), btnLoadFile);
        pulseDown.setToX(1.0);
        pulseDown.setToY(1.0);

        pulseAnimation = new SequentialTransition(
                new PauseTransition(Duration.seconds(2.0)),
                pulseUp, pulseDown
        );
        pulseAnimation.setCycleCount(Animation.INDEFINITE);

        introAnimation = new SequentialTransition(
                new PauseTransition(Duration.millis(200)),
                new ParallelTransition(fade)
        );
        introAnimation.setOnFinished(e -> pulseAnimation.play());
        introAnimation.play();
    }

    private void pulseRunAndDebugButtons() {
        if (!animationsEnabled.get()) return;
        if (runDebugPulse != null) runDebugPulse.stop();

        DropShadow ds = new DropShadow(30, Color.rgb(0,0,0,0.30));
        btnRun.setEffect(ds);
        btnDebug.setEffect(ds);

        double scaleUpRatio = 1.08;
        Duration time = Duration.millis(350);

        ScaleTransition runUp   = new ScaleTransition(time, btnRun);
        runUp.setToX(scaleUpRatio);
        runUp.setToY(scaleUpRatio);
        ScaleTransition runDown = new ScaleTransition(time, btnRun);
        runDown.setToX(1);
        runDown.setToY(1);

        ScaleTransition dbgUp   = new ScaleTransition(time, btnDebug);
        dbgUp.setToX(scaleUpRatio);
        dbgUp.setToY(scaleUpRatio);
        ScaleTransition dbgDown = new ScaleTransition(time, btnDebug);
        dbgDown.setToX(1);
        dbgDown.setToY(1);

        SequentialTransition runSeq = new SequentialTransition(runUp, runDown);
        SequentialTransition dbgSeq = new SequentialTransition(dbgUp, dbgDown);

        runDebugPulse = new ParallelTransition(runSeq, dbgSeq);
        runDebugPulse.setInterpolator(Interpolator.EASE_BOTH);
        runDebugPulse.play();
    }

    private void stopAllAnimations() {
        if (pulseAnimation != null) pulseAnimation.stop();
        if (introAnimation != null) introAnimation.stop();
        if (runDebugPulse != null) runDebugPulse.stop();
    }

    private void applySelectedTheme(String themeKey) {
        if (themeKey == null) themeKey = Theme.LIGHT.toString();
        if (btnRun == null || btnRun.getScene() == null) return;

        var scene = btnRun.getScene();
        var sheets = scene.getStylesheets();
        sheets.clear();

        addCss(sheets, "/ui/styles/light.css"); // base
        String themePath = switch (themeKey) {
            case "Dark" -> "/ui/styles/dark.css";
            case "Yellow-Blue" -> "/ui/styles/yellowblue.css";
            default -> "/ui/styles/light.css";
        };
        addCss(sheets, themePath);

        // Tag the tables with the theme key so the row highlight rules match
        var table = mainInstrTableController.getTable();
        var historyTable = historyInstrTableController.getTable(); // ensure controller exposes getTable()

        String cls = themeKey.equals("Dark") ? "dark"
                : themeKey.equals("Yellow-Blue") ? "yellowblue"
                : "light";

        if (table != null) {
            table.getStyleClass().removeAll("light","dark","yellowblue");
            table.getStyleClass().add(cls);
            table.refresh();
        }
        if (historyTable != null) {
            historyTable.getStyleClass().removeAll("light","dark","yellowblue");
            historyTable.getStyleClass().add(cls);
            historyTable.refresh();
        }
    }

    private void addCss(List<String> sheets, String path) {
        var url = getClass().getResource(path);
        if (url == null) {
            System.err.println("CSS not found on classpath: " + path);
            return;
        }
        sheets.add(url.toExternalForm());
    }
}