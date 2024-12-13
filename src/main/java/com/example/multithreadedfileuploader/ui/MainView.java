/**
 * Controller class for the main user interface of the Multithreaded File Uploader application.
 * <p>
 * This class manages the interaction between the user interface (FXML view) and the backend logic.
 * It allows users to select files for upload, start the upload process, pause, resume, or cancel uploads.
 * Additionally, it supports managing multiple upload sessions concurrently.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Dynamic creation of upload sections for multiple files.</li>
 *   <li>Control over individual uploads (start, pause, resume, cancel).</li>
 *   <li>Real-time progress tracking for each file.</li>
 *   <li>Integration with the backend service to handle file uploads.</li>
 * </ul>
 */
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
    private boolean isDefaultSectionUsed = false;

    /**
     * Initializes the user interface and sets up event listeners for the controls.
     * <p>
     * Handles file selection and dynamically creates new upload sections for multiple files.
     * </p>
     */
    @FXML
    public void initialize() {
        selectFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File for Upload");
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                if (!isDefaultSectionUsed) {
                    useDefaultSection(selectedFile);
                } else {
                    addUploadSection(selectedFile);
                }
            } else {
                logger.warning("No file selected.");
            }
        });
    }

    /**
     * Configures the default upload section for the first selected file.
     *
     * @param file The file selected for upload.
     */
    private void useDefaultSection(File file) {
        if (isDefaultSectionUsed) {
            resetDefaultSection();
        }

        filePathLabel.setText("Selected: " + file.getAbsolutePath());

        UploadState state = new UploadState();
        uploadStates.put(file, state);

        startUploadButton.setOnAction(event -> startUpload(file, uploadProgressBar, progressLabel, state, startUploadButton, pauseUploadButton, resumeUploadButton, cancelUploadButton));
        pauseUploadButton.setOnAction(event -> pauseUpload(file, state, pauseUploadButton, resumeUploadButton));
        resumeUploadButton.setOnAction(event -> resumeUpload(file, state, pauseUploadButton, resumeUploadButton));
        cancelUploadButton.setOnAction(event -> cancelUpload(file, state, defaultUploadSection, true));

        toggleButtons(true, false, false, false, startUploadButton, pauseUploadButton, resumeUploadButton, cancelUploadButton);

        isDefaultSectionUsed = true;
    }

    /**
     * Dynamically creates a new upload section for additional files.
     *
     * @param file The file selected for upload.
     */
    private void addUploadSection(File file) {
        Label newFilePathLabel = new Label("Selected: " + file.getAbsolutePath());
        newFilePathLabel.setStyle("-fx-text-fill: grey;");

        ProgressBar newProgressBar = new ProgressBar(0);
        newProgressBar.setPrefWidth(uploadProgressBar.getPrefWidth());

        Label newProgressLabel = new Label("Progress: 0%");
        newProgressLabel.setStyle("-fx-text-fill: grey;");

        Button newStartButton = new Button("Start Upload");
        newStartButton.setStyle(startUploadButton.getStyle());
        Button newPauseButton = new Button("Pause Upload");
        newPauseButton.setStyle(pauseUploadButton.getStyle());
        Button newResumeButton = new Button("Resume Upload");
        newResumeButton.setStyle(resumeUploadButton.getStyle());
        Button newCancelButton = new Button("Cancel Upload");
        newCancelButton.setStyle(cancelUploadButton.getStyle());

        HBox buttonBox = new HBox(10, newStartButton, newPauseButton, newResumeButton, newCancelButton);
        buttonBox.setSpacing(10);

        VBox newUploadSection = new VBox(10, newFilePathLabel, newProgressBar, newProgressLabel, buttonBox);
        newUploadSection.setSpacing(15);
        newUploadSection.setStyle(defaultUploadSection.getStyle());

        Platform.runLater(() -> uploadContainer.getChildren().add(newUploadSection));

        UploadState state = new UploadState();
        uploadStates.put(file, state);

        newStartButton.setOnAction(event -> startUpload(file, newProgressBar, newProgressLabel, state, newStartButton, newPauseButton, newResumeButton, newCancelButton));
        newPauseButton.setOnAction(event -> pauseUpload(file, state, newPauseButton, newResumeButton));
        newResumeButton.setOnAction(event -> resumeUpload(file, state, newPauseButton, newResumeButton));
        newCancelButton.setOnAction(event -> cancelUpload(file, state, newUploadSection, false));
    }

    /**
     * Starts the upload process for a selected file.
     *
     * @param file The file to upload.
     * @param progressBar The progress bar associated with this upload.
     * @param progressLabel The label displaying progress percentage.
     * @param state The upload state for the file.
     * @param startButton The start button for this upload.
     * @param pauseButton The pause button for this upload.
     * @param resumeButton The resume button for this upload.
     * @param cancelButton The cancel button for this upload.
     */
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

    /**
     * Pauses the ongoing upload for a file.
     *
     * @param file The file being uploaded.
     * @param state The upload state for the file.
     * @param pauseButton The pause button for this upload.
     * @param resumeButton The resume button for this upload.
     */
    private void pauseUpload(File file, UploadState state, Button pauseButton, Button resumeButton) {
        if (state.isUploading() && !state.isPaused()) {
            state.setPaused(true);
            toggleButtons(false, false, true, true, null, pauseButton, resumeButton, null);
        } else {
            logger.warning("Pause Upload failed: No active upload or already paused.");
        }
    }

    /**
     * Resumes a paused upload for a file.
     *
     * @param file The file being uploaded.
     * @param state The upload state for the file.
     * @param pauseButton The pause button for this upload.
     * @param resumeButton The resume button for this upload.
     */
    private void resumeUpload(File file, UploadState state, Button pauseButton, Button resumeButton) {
        if (state.isUploading() && state.isPaused()) {
            state.setPaused(false);
            toggleButtons(false, true, false, true, null, pauseButton, resumeButton, null);
        } else {
            logger.warning("Resume Upload failed: Upload not paused or not active.");
        }
    }

    /**
     * Cancels the ongoing upload for a file and resets the upload section.
     *
     * @param file The file being uploaded.
     * @param state The upload state for the file.
     * @param uploadSection The upload section for this file.
     * @param isDefault Indicates whether this is the default upload section.
     */
    private void cancelUpload(File file, UploadState state, VBox uploadSection, boolean isDefault) {
        if (state.isUploading()) {
            state.setCancelled(true);
            Thread uploadThread = state.getUploadThread();
            if (uploadThread != null) {
                uploadThread.interrupt();
            }

            if (!isDefault) {
                Platform.runLater(() -> uploadContainer.getChildren().remove(uploadSection));
            } else {
                resetDefaultSection();
            }

            fileUploadService.cleanupCanceledUpload(file);

            uploadStates.remove(file);

            toggleButtons(true, false, false, false, startUploadButton, pauseUploadButton, resumeUploadButton, cancelUploadButton);
        } else {
            logger.warning("Cancel Upload failed: No active upload thread.");
        }
    }

    /**
     * Resets the default upload section to its initial state.
     *
     * This method clears the file path label, progress label, and progress bar,
     * and disables all buttons in the default upload section. It also resets the
     * `isDefaultSectionUsed` flag to indicate that the default section is available
     * for new uploads.
     */
    private void resetDefaultSection() {
        filePathLabel.setText("No file selected");

        progressLabel.setText("Progress: 0%");
        uploadProgressBar.setProgress(0);

        toggleButtons(false, false, false, false, startUploadButton, pauseUploadButton, resumeUploadButton, cancelUploadButton);

        isDefaultSectionUsed = false;
    }

    /**
     * Toggles the enabled state of the action buttons for an upload section.
     *
     * This method is used to dynamically enable or disable buttons such as Start,
     * Pause, Resume, and Cancel for a specific upload section based on the provided
     * parameters.
     *
     * @param startEnabled Indicates whether the Start button should be enabled.
     * @param pauseEnabled Indicates whether the Pause button should be enabled.
     * @param resumeEnabled Indicates whether the Resume button should be enabled.
     * @param cancelEnabled Indicates whether the Cancel button should be enabled.
     * @param startButton The Start button to toggle.
     * @param pauseButton The Pause button to toggle.
     * @param resumeButton The Resume button to toggle.
     * @param cancelButton The Cancel button to toggle.
     */
    private void toggleButtons(boolean startEnabled, boolean pauseEnabled, boolean resumeEnabled, boolean cancelEnabled,
                               Button startButton, Button pauseButton, Button resumeButton, Button cancelButton) {
        if (startButton != null) startButton.setDisable(!startEnabled);
        if (pauseButton != null) pauseButton.setDisable(!pauseEnabled);
        if (resumeButton != null) resumeButton.setDisable(!resumeEnabled);
        if (cancelButton != null) cancelButton.setDisable(!cancelEnabled);
    }

    /**
     * Represents the state of an ongoing upload.
     *
     * This inner class encapsulates the status of a file upload, including whether
     * it is paused, canceled, or currently in progress. It also holds a reference
     * to the upload thread handling the file upload.
     */
    private static class UploadState {
        private volatile boolean isPaused = false;
        private volatile boolean isCancelled = false;
        private volatile boolean isUploading = false;
        private Thread uploadThread;

        /**
         * Checks whether the upload is paused.
         *
         * @return true if the upload is paused, false otherwise.
         */
        public boolean isPaused() {
            return isPaused;
        }

        /**
         * Sets the paused state of the upload.
         *
         * @param paused true to pause the upload, false to resume it.
         */
        public void setPaused(boolean paused) {
            isPaused = paused;
        }

        /**
         * Checks whether the upload is canceled.
         *
         * @return true if the upload is canceled, false otherwise.
         */
        public boolean isCancelled() {
            return isCancelled;
        }

        /**
         * Sets the canceled state of the upload.
         *
         * @param cancelled true to cancel the upload, false otherwise.
         */
        public void setCancelled(boolean cancelled) {
            isCancelled = cancelled;
        }

        /**
         * Checks whether the upload is currently in progress.
         *
         * @return true if the upload is in progress, false otherwise.
         */
        public boolean isUploading() {
            return isUploading;
        }

        /**
         * Sets the uploading state of the upload.
         *
         * @param uploading true to mark the upload as in progress, false otherwise.
         */
        public void setUploading(boolean uploading) {
            isUploading = uploading;
        }

        /**
         * Retrieves the thread handling the file upload.
         *
         * @return The upload thread.
         */
        public Thread getUploadThread() {
            return uploadThread;
        }

        /**
         * Sets the thread handling the file upload.
         *
         * @param uploadThread The upload thread to set.
         */
        public void setUploadThread(Thread uploadThread) {
            this.uploadThread = uploadThread;
        }
    }
}
