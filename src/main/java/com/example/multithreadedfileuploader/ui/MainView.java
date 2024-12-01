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
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

@Component
public class MainView {

    private static final Logger logger = Logger.getLogger(MainView.class.getName());

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
    private Button pauseUploadButton;

    @FXML
    private Button resumeUploadButton;

    private File selectedFile;

    @Autowired
    private FileUploadService fileUploadService;

    private Thread uploadThread;

    @FXML
    private Label progressLabel;

    private volatile boolean isPaused = false; // Track if the upload is paused
    private volatile boolean isCancelled = false; // Track if the upload is cancelled
    private volatile boolean isUploading = false; // Track if an upload is active
    private Timer progressFetchTimer; // Timer to fetch progress from the database

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
                uploadProgressBar.setProgress(0);
                toggleButtons(true, false, false, false); // Enable only Start Upload
            } else {
                filePathLabel.setText("No file selected");
            }
        });

        // Handle start upload
        startUploadButton.setOnAction(event -> {
            if (selectedFile != null) {
                isPaused = false;
                isCancelled = false;
                isUploading = true;

                uploadThread = new Thread(() -> {
                    fileUploadService.uploadFile(
                            selectedFile,
                            progress -> Platform.runLater(() -> {
                                uploadProgressBar.setProgress(progress);
                                progressLabel.setText("Progress: " + (int) (progress * 100) + "%");
                            }),
                            status -> Platform.runLater(() -> {
                                filePathLabel.setText(status);
                                if (status.equals("Upload Completed!") || status.equals("Upload Cancelled")) {
                                    progressLabel.setText(status);
                                    isUploading = false;
                                    stopProgressFetching();
                                    toggleButtons(true, false, false, false); // Reset buttons
                                }
                            }),
                            () -> isPaused,
                            () -> isCancelled
                    );
                });
                uploadThread.start();
                startProgressFetching(); // Start fetching progress from the database
                toggleButtons(false, true, false, true); // Enable Pause and Cancel
            } else {
                filePathLabel.setText("Please select a file first!");
            }
        });

        // Handle pause upload
        pauseUploadButton.setOnAction(event -> {
            if (isUploading && !isPaused) {
                isPaused = true;
                Platform.runLater(() -> {
                    filePathLabel.setText("Upload Paused");
                    toggleButtons(false, false, true, true); // Enable Resume and Cancel
                });
            } else {
                logger.warning("Pause Upload failed: No active upload or already paused.");
            }
        });

        // Handle resume upload
        resumeUploadButton.setOnAction(event -> {
            if (isUploading && isPaused) {
                isPaused = false;
                Platform.runLater(() -> {
                    filePathLabel.setText("Resuming Upload...");
                    toggleButtons(false, true, false, true); // Enable Pause and Cancel
                });
            } else {
                logger.warning("Resume Upload failed: Upload not paused or not active.");
            }
        });

        // Handle cancel upload
        cancelUploadButton.setOnAction(event -> {
            if (isUploading) {
                isCancelled = true;
                uploadThread.interrupt(); // Stop the thread
                stopProgressFetching(); // Stop progress fetching
                Platform.runLater(() -> {
                    uploadProgressBar.setProgress(0);
                    filePathLabel.setText("Upload Cancelled");
                    progressLabel.setText("Progress: 0%");
                    isUploading = false;
                    toggleButtons(true, false, false, false); // Reset buttons
                });
            } else {
                logger.warning("Cancel Upload failed: No active upload thread.");
            }
        });
    }

    private void toggleButtons(boolean startEnabled, boolean pauseEnabled, boolean resumeEnabled, boolean cancelEnabled) {
        startUploadButton.setDisable(!startEnabled);
        pauseUploadButton.setDisable(!pauseEnabled);
        resumeUploadButton.setDisable(!resumeEnabled);
        cancelUploadButton.setDisable(!cancelEnabled);
    }

    private void startProgressFetching() {
        progressFetchTimer = new Timer(true);
        progressFetchTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Fetch real-time progress from the database
                fileUploadService.fetchProgress(selectedFile.getName(),
                        progress -> Platform.runLater(() -> {
                            uploadProgressBar.setProgress(progress);
                            progressLabel.setText("Real-time Progress: " + (int) (progress * 100) + "%");
                        }),
                        status -> Platform.runLater(() -> filePathLabel.setText("Real-time Status: " + status))
                );
            }
        }, 0, 1000); // Fetch progress every second
    }

    private void stopProgressFetching() {
        if (progressFetchTimer != null) {
            progressFetchTimer.cancel();
            progressFetchTimer = null;
        }
    }
}
