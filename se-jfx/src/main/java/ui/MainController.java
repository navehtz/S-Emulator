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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import ui.components.DynamicInputsController;
import ui.components.InstructionTableController;
import ui.components.RunHistoryTableController;
import ui.components.VariablesTableController;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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


    private final Engine engine = new EngineImpl();
    private final ObjectProperty<ProgramDTO> currentProgramDTO = new SimpleObjectProperty<>() {};
    public ReadOnlyObjectProperty<ProgramDTO> currentProgramProperty() { return currentProgramDTO; }

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
            }
        });

        btnRun.disableProperty().bind(
                currentProgramProperty().isNull()
        );
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
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(window);
            alert.setTitle("No File Selected");
            alert.setHeaderText(null);
            alert.setContentText("No file was selected. Please choose an XML file.\n");
            alert.showAndWait();
            return;
        }

        programNameLabel.setText(selectedFile.getAbsolutePath());
        engine.loadProgram(Path.of(selectedFile.getAbsolutePath()));

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


}
