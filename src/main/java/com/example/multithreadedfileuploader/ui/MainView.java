package com.example.multithreadedfileuploader.ui;

import com.example.multithreadedfileuploader.service.FileUploadService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class MainView {

    @FXML
    private Button selectFileButton;

    @FXML
    private Label filePathLabel;

    @FXML
    private ProgressBar uploadProgressBar;

    @FXML
    private Button startUploadButton;

    @FXML
    private Button cancelUploadButton;

    @FXML
    private Label progressLabel;

    private File selectedFile;

    @Autowired
    private FileUploadService fileUploadService;

    private Thread uploadThread;

    @FXML
    public void initialize() {
        // Handle file selection
        selectFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File for Upload");
            selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                filePathLabel.setText("Selected: " + selectedFile.getAbsolutePath());
                progressLabel.setText("Progress: 0%");
            } else {
                filePathLabel.setText("No file selected");
            }
        });

        // Handle start upload
        startUploadButton.setOnAction(event -> {
            if (selectedFile != null) {
                if (!selectedFile.exists() || !selectedFile.canRead()) {
                    filePathLabel.setText("Error: File is not accessible!");
                    return;
                }

                // Reset progress
                uploadProgressBar.setProgress(0);
                progressLabel.setText("Progress: 0%");
                filePathLabel.setText("Uploading: " + selectedFile.getName());

                // Start upload in a new thread
                uploadThread = new Thread(() -> {
                    fileUploadService.uploadFile(
                            selectedFile,
                            progress -> Platform.runLater(() -> {
                                uploadProgressBar.setProgress(progress);
                                progressLabel.setText("Progress: " + (int) (progress * 100) + "%");
                            }),
                            status -> Platform.runLater(() -> {
                                filePathLabel.setText(status);
                                progressLabel.setText("Progress: 100%");
                            })
                    );
                });
                uploadThread.start();
            } else {
                filePathLabel.setText("Please select a file first!");
            }
        });

        // Handle cancel upload
        cancelUploadButton.setOnAction(event -> {
            if (uploadThread != null && uploadThread.isAlive()) {
                uploadThread.interrupt(); // Stop the thread
                Platform.runLater(() -> {
                    uploadProgressBar.setProgress(0);
                    filePathLabel.setText("Upload canceled.");
                    progressLabel.setText("Progress: 0%");
                });
            }
        });
    }
}
