package com.example.multithreadedfileuploader.ui;

import com.example.multithreadedfileuploader.service.FileUploadService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Component
public class MainView {

    private static final Logger logger = Logger.getLogger(MainView.class.getName());

    @FXML
    private Button selectFileButton;

    @FXML
    private VBox uploadContainer;

    @FXML
    private VBox defaultUploadSection;

    @FXML
    private Label filePathLabel;

    @FXML
    private ProgressBar uploadProgressBar;

    @FXML
    private Label progressLabel;

    @FXML
    private Button startUploadButton;

    @FXML
    private Button pauseUploadButton;

    @FXML
    private Button resumeUploadButton;

    @FXML
    private Button cancelUploadButton;

    @Autowired
    private FileUploadService fileUploadService;

    private final Map<File, UploadState> uploadStates = new HashMap<>();
    private boolean isDefaultSectionUsed = false; // Track if default section is in use

    @FXML
    public void initialize() {
        // Handle file selection
        selectFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File for Upload");
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                if (!isDefaultSectionUsed) {
                    useDefaultSection(selectedFile); // Use default section for the first file
                } else {
                    addUploadSection(selectedFile); // Create a new section for additional files
                }
            } else {
                logger.warning("No file selected.");
            }
        });
    }

    private void useDefaultSection(File file) {
        // Check if default section is already in use
        if (isDefaultSectionUsed) {
            resetDefaultSection(); // Reset before reusing
        }

        // Set the filePathLabel to the new file
        filePathLabel.setText("Selected: " + file.getAbsolutePath());

        // Initialize the UploadState for the new file
        UploadState state = new UploadState();
        uploadStates.put(file, state);

        // Set button actions for the new file
        startUploadButton.setOnAction(event -> startUpload(file, uploadProgressBar, progressLabel, state, startUploadButton, pauseUploadButton, resumeUploadButton, cancelUploadButton));
        pauseUploadButton.setOnAction(event -> pauseUpload(file, state, pauseUploadButton, resumeUploadButton));
        resumeUploadButton.setOnAction(event -> resumeUpload(file, state, pauseUploadButton, resumeUploadButton));
        cancelUploadButton.setOnAction(event -> cancelUpload(file, state, defaultUploadSection, true));

        // Enable the "Start Upload" button and disable others
        toggleButtons(true, false, false, false, startUploadButton, pauseUploadButton, resumeUploadButton, cancelUploadButton);

        // Mark the default section as used
        isDefaultSectionUsed = true;
    }


    private void addUploadSection(File file) {
        // Create UI components for the new upload section
        Label newFilePathLabel = new Label("Selected: " + file.getAbsolutePath());
        newFilePathLabel.setStyle("-fx-text-fill: grey;"); // Apply style matching the original

        ProgressBar newProgressBar = new ProgressBar(0);
        newProgressBar.setPrefWidth(uploadProgressBar.getPrefWidth()); // Match the width of the original

        Label newProgressLabel = new Label("Progress: 0%");
        newProgressLabel.setStyle("-fx-text-fill: grey;"); // Match the style of the original

        Button newStartButton = new Button("Start Upload");
        newStartButton.setStyle(startUploadButton.getStyle()); // Apply original button styles
        Button newPauseButton = new Button("Pause Upload");
        newPauseButton.setStyle(pauseUploadButton.getStyle());
        Button newResumeButton = new Button("Resume Upload");
        newResumeButton.setStyle(resumeUploadButton.getStyle());
        Button newCancelButton = new Button("Cancel Upload");
        newCancelButton.setStyle(cancelUploadButton.getStyle());

        // Create an HBox for buttons with spacing
        HBox buttonBox = new HBox(10, newStartButton, newPauseButton, newResumeButton, newCancelButton);
        buttonBox.setSpacing(10);

        // Combine all components into a VBox for the new section
        VBox newUploadSection = new VBox(10, newFilePathLabel, newProgressBar, newProgressLabel, buttonBox);
        newUploadSection.setSpacing(15); // Match spacing
        newUploadSection.setStyle(defaultUploadSection.getStyle()); // Apply the same style as the default section

        // Add the new section to the upload container
        Platform.runLater(() -> uploadContainer.getChildren().add(newUploadSection));

        // Initialize upload state for the new file
        UploadState state = new UploadState();
        uploadStates.put(file, state);

        // Set button actions for the new section
        newStartButton.setOnAction(event -> startUpload(file, newProgressBar, newProgressLabel, state, newStartButton, newPauseButton, newResumeButton, newCancelButton));
        newPauseButton.setOnAction(event -> pauseUpload(file, state, newPauseButton, newResumeButton));
        newResumeButton.setOnAction(event -> resumeUpload(file, state, newPauseButton, newResumeButton));
        newCancelButton.setOnAction(event -> cancelUpload(file, state, newUploadSection, false));
    }

    private void startUpload(File file, ProgressBar progressBar, Label progressLabel, UploadState state,
                             Button startButton, Button pauseButton, Button resumeButton, Button cancelButton) {
        if (!state.isUploading()) {
            state.setPaused(false);
            state.setCancelled(false);
            state.setUploading(true);

            Thread uploadThread = new Thread(() -> {
                fileUploadService.uploadFile(
                        file,
                        progress -> Platform.runLater(() -> {
                            progressBar.setProgress(progress);
                            progressLabel.setText("Progress: " + (int) (progress * 100) + "%");
                        }),
                        status -> Platform.runLater(() -> {
                            progressLabel.setText(status);
                            if (status.equals("Upload Completed!") || status.equals("Upload Cancelled")) {
                                state.setUploading(false);
                                toggleButtons(false, false, false, false, startButton, pauseButton, resumeButton, cancelButton);
                            }
                        }),
                        state::isPaused,
                        state::isCancelled
                );
            });
            state.setUploadThread(uploadThread);
            uploadThread.start();

            toggleButtons(false, true, false, true, startButton, pauseButton, resumeButton, cancelButton);
        }
    }

    private void pauseUpload(File file, UploadState state, Button pauseButton, Button resumeButton) {
        if (state.isUploading() && !state.isPaused()) {
            state.setPaused(true);
            toggleButtons(false, false, true, true, null, pauseButton, resumeButton, null);
        } else {
            logger.warning("Pause Upload failed: No active upload or already paused.");
        }
    }

    private void resumeUpload(File file, UploadState state, Button pauseButton, Button resumeButton) {
        if (state.isUploading() && state.isPaused()) {
            state.setPaused(false);
            toggleButtons(false, true, false, true, null, pauseButton, resumeButton, null);
        } else {
            logger.warning("Resume Upload failed: Upload not paused or not active.");
        }
    }

    private void cancelUpload(File file, UploadState state, VBox uploadSection, boolean isDefault) {
        if (state.isUploading()) {
            state.setCancelled(true);
            Thread uploadThread = state.getUploadThread();
            if (uploadThread != null) {
                uploadThread.interrupt(); // Safely interrupt the thread
            }

            // If it's not the default section, remove it from the UI
            if (!isDefault) {
                Platform.runLater(() -> uploadContainer.getChildren().remove(uploadSection));
            } else {
                resetDefaultSection(); // Properly reset the default section
            }

            // Cleanup the database records
            fileUploadService.cleanupCanceledUpload(file);

            // Remove the file's state from the tracking map
            uploadStates.remove(file);

            // Re-enable the "Start Upload" button
            toggleButtons(true, false, false, false, startUploadButton, pauseUploadButton, resumeUploadButton, cancelUploadButton);
        } else {
            logger.warning("Cancel Upload failed: No active upload thread.");
        }
    }



    private void resetDefaultSection() {
        // Reset the filePathLabel
        filePathLabel.setText("No file selected");

        // Reset the progressLabel and progress bar
        progressLabel.setText("Progress: 0%");
        uploadProgressBar.setProgress(0);

        // Disable all buttons except Select File
        toggleButtons(false, false, false, false, startUploadButton, pauseUploadButton, resumeUploadButton, cancelUploadButton);

        // Clear the UploadState associated with the default section
        isDefaultSectionUsed = false;
    }


    private void toggleButtons(boolean startEnabled, boolean pauseEnabled, boolean resumeEnabled, boolean cancelEnabled,
                               Button startButton, Button pauseButton, Button resumeButton, Button cancelButton) {
        if (startButton != null) startButton.setDisable(!startEnabled);
        if (pauseButton != null) pauseButton.setDisable(!pauseEnabled);
        if (resumeButton != null) resumeButton.setDisable(!resumeEnabled);
        if (cancelButton != null) cancelButton.setDisable(!cancelEnabled);
    }


    private static class UploadState {
        private volatile boolean isPaused = false;
        private volatile boolean isCancelled = false;
        private volatile boolean isUploading = false;
        private Thread uploadThread;

        public boolean isPaused() {
            return isPaused;
        }

        public void setPaused(boolean paused) {
            isPaused = paused;
        }

        public boolean isCancelled() {
            return isCancelled;
        }

        public void setCancelled(boolean cancelled) {
            isCancelled = cancelled;
        }

        public boolean isUploading() {
            return isUploading;
        }

        public void setUploading(boolean uploading) {
            isUploading = uploading;
        }

        public Thread getUploadThread() {
            return uploadThread;
        }

        public void setUploadThread(Thread uploadThread) {
            this.uploadThread = uploadThread;
        }
    }
}
