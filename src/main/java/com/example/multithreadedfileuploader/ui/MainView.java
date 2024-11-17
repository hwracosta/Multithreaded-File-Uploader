package com.example.multithreadedfileuploader.ui;

import com.example.multithreadedfileuploader.service.FileUploadService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;

import java.io.File;

public class MainView {
    @FXML
    private Button selectFileButton;

    @FXML
    private Label filePathLabel;

    @FXML
    private ProgressBar uploadProgressBar;

    @FXML
    private Button startUploadButton;

    private File selectedFile;
    private final FileUploadService fileUploadService = new FileUploadService();

    @FXML
    public void initialize() {
        // Handle file selection
        selectFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File for Upload");
            selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                filePathLabel.setText(selectedFile.getAbsolutePath());
            } else {
                filePathLabel.setText("No file selected");
            }
        });

        // Handle start upload button click
        startUploadButton.setOnAction(event -> {
            if (selectedFile != null) {
                uploadProgressBar.setProgress(0); // Reset progress
                fileUploadService.uploadFile(selectedFile, uploadProgressBar);
            } else {
                filePathLabel.setText("Please select a file first!");
            }
        });
    }
}
