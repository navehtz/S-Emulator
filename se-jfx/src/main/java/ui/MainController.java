// src/main/java/ui/MainController.java
package ui;

import dto.InstructionDTO;
import dto.ProgramDTO;
import engine.Engine;
import engine.EngineImpl;
import exceptions.EngineLoadException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import ui.components.DynamicInputsController;
import ui.components.InstructionTableController;
import ui.components.RunHistoryTableController;
import ui.components.VariablesTableController;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

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


    private final Engine engine = new EngineImpl();
    private ProgramDTO currentProgramDTO;

    @FXML
    private void initialize() {
        mainInstrTableController.getTable().getSelectionModel()
                .selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
                    int i = (newIdx == null) ? -1 : newIdx.intValue();
                    if (i >= 0 && currentProgramDTO != null &&
                            i < currentProgramDTO.expandedProgram().size()) {

                        List<InstructionDTO> chain = currentProgramDTO.expandedProgram().get(i).reversed();
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
    }

    private void updateCurrentProgramAndMainInstrTable(ProgramDTO dto) {
        this.currentProgramDTO = dto;
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
    }

    @FXML private void onRun(ActionEvent e)        { System.out.println("Run"); }
    @FXML private void onDebug(ActionEvent e)      { System.out.println("Debug"); }
    @FXML private void onStop(ActionEvent e)       { System.out.println("Stop"); }
    @FXML private void onResume(ActionEvent e)     { System.out.println("Resume"); }
    @FXML private void onStepOver(ActionEvent e)   { System.out.println("Step Over"); }

    // If your toolbar has other onAction handlers, add them here too.
}
