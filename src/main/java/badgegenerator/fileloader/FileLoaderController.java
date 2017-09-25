package badgegenerator.fileloader;

import badgegenerator.ModelSingleton;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

import static java.lang.Thread.sleep;

public class FileLoaderController {
    @FXML
    private Rectangle progressIndicatorBackground;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private TextField xlsxField;
    @FXML
    private TextField pdfField;
    @FXML
    private CheckBox hasHeadingsCheckBox;
    @FXML
    private Label xlsxNotLoadedLabel;
    @FXML
    private Label pdfNotLoadedLabel;

    public void handleBrowseXlsx(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XLSX files", "*.xlsx"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if(selectedFile != null) {
            xlsxField.setText(selectedFile.getName());
            ModelSingleton.getInstance().setSrcXlsxPath(selectedFile.getAbsolutePath());
        } else {
            System.out.println("File is incorrect");
        }
    }

    public void handleBrowsePdf(MouseEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if(selectedFile != null) {
            pdfField.setText(selectedFile.getName());
            ModelSingleton.getInstance().setSrcPdfPath(selectedFile.getAbsolutePath());
        } else {
            System.out.println("File is incorrect");
        }
    }

    public void handleProceed(MouseEvent event) throws IOException {
        if(checkIfFieldsAreFilled()) {
            progressIndicator.setVisible(true);
            progressIndicatorBackground.setVisible(true);
            final Stage pdfRedactorWindow = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Task checkExcelReaderTask = new CheckExcelReaderTask();
            checkExcelReaderTask.setOnSucceeded(e -> {
                if((boolean) checkExcelReaderTask.getValue()) {
                    Task prepareParentTask = new PrepareParentTask("/fxml/PdfEditor.fxml");
                    prepareParentTask.setOnSucceeded(event1 -> {
                        progressIndicator.setVisible(false);
                        progressIndicatorBackground.setVisible(false);
                        pdfRedactorWindow.setScene(new Scene((Parent) prepareParentTask.getValue()));
                        pdfRedactorWindow.setResizable(false);
                        pdfRedactorWindow.show();
                    });
                    Thread thread = new Thread(prepareParentTask);
                    thread.setDaemon(true);
                    thread.start();
                } else {
                    progressIndicator.setVisible(false);
                    progressIndicatorBackground.setVisible(false);
                }
            });

            Thread thread = new Thread(checkExcelReaderTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private boolean checkIfFieldsAreFilled() {
        boolean fieldsAreFilled = true;
        if(pdfField.getText().isEmpty()) {
            fieldsAreFilled = false;
            pdfNotLoadedLabel.setVisible(true);
        } else pdfNotLoadedLabel.setVisible(false);
        if(xlsxField.getText().isEmpty()){
            fieldsAreFilled = false;
            xlsxNotLoadedLabel.setVisible(true);
        } else xlsxNotLoadedLabel.setVisible(false);
        return fieldsAreFilled;
    }

    public void handleSetHasHeadings(MouseEvent event) {
        ModelSingleton.getInstance().setHasHeadings(hasHeadingsCheckBox.isSelected());
    }
}
