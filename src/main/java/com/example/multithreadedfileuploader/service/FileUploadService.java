package com.example.multithreadedfileuploader.service;

import com.example.multithreadedfileuploader.entity.ChunkMetadata;
import com.example.multithreadedfileuploader.entity.FileMetadata;
import com.example.multithreadedfileuploader.repository.ChunkMetadataRepository;
import com.example.multithreadedfileuploader.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Service
public class FileUploadService {

    private static final Logger logger = Logger.getLogger(FileUploadService.class.getName());

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private ChunkMetadataRepository chunkMetadataRepository;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private volatile boolean isPaused = false;
    private volatile boolean isCancelled = false;
    private volatile boolean isUploading = false; // To track upload state
    private volatile File currentFile = null; // To track the current file

    public void uploadFile(
            File file,
            Consumer<Double> progressCallback,
            Consumer<String> statusCallback,
            BooleanSupplier pauseCheck,
            BooleanSupplier cancelCheck
    ) {
        // Reset progress and upload state when a new file is selected
        if (currentFile != null && !currentFile.equals(file)) {
            resetUploadState();  // Reset the state when a new file is selected
            resetProgress(progressCallback);  // Reset progress bar to 0
        }

        currentFile = file;  // Track the new file

        // If upload was canceled or no upload is ongoing, reset progress and state
        if (isCancelled || !isUploading) {
            resetProgress(progressCallback);
        }

        isUploading = true;  // Set upload state to in progress

        executorService.submit(() -> {
            try {
                final long CHUNK_SIZE = 1024 * 1024; // 1 MB
                long totalChunks = (file.length() + CHUNK_SIZE - 1) / CHUNK_SIZE;

                FileMetadata metadata = fileMetadataRepository.findByFileName(file.getName())
                        .orElseGet(() -> {
                            FileMetadata newMetadata = new FileMetadata();
                            newMetadata.setFileName(file.getName());
                            newMetadata.setFileSize(file.length());
                            newMetadata.setStatus("Uploading");
                            newMetadata.setUploadedChunks(0);
                            newMetadata.setTotalChunks((int) totalChunks);
                            return fileMetadataRepository.save(newMetadata);
                        });

                if (chunkMetadataRepository.findByFileId(metadata.getId()).isEmpty()) {
                    for (int i = 0; i < totalChunks; i++) {
                        chunkMetadataRepository.save(new ChunkMetadata(metadata.getId(), i, "Pending", 0.0));
                    }
                }

                for (int i = metadata.getUploadedChunks(); i < totalChunks; i++) {
                    if (cancelCheck.getAsBoolean()) {
                        // If cancel is pressed, reset progress and stop upload
                        statusCallback.accept("Upload Cancelled");
                        metadata.setStatus("Cancelled");
                        fileMetadataRepository.save(metadata);
                        resetProgress(progressCallback);  // Reset progress on cancel
                        isUploading = false;  // Reset upload state
                        return;
                    }

                    while (pauseCheck.getAsBoolean()) {
                        Thread.sleep(500);
                    }

                    Thread.sleep(100);  // Simulate file chunk processing delay

                    double progress = (double) (i + 1) / totalChunks;
                    progressCallback.accept(progress);

                    metadata.setUploadedChunks(i + 1);
                    fileMetadataRepository.save(metadata);

                    ChunkMetadata chunk = chunkMetadataRepository.findByFileIdAndChunkNumber(metadata.getId(), i)
                            .orElseThrow(() -> new IllegalStateException("Chunk metadata not found"));

                    chunk.setStatus("Completed");
                    chunk.setProgress(100.0);
                    chunkMetadataRepository.save(chunk);
                }

                metadata.setStatus("Completed");
                fileMetadataRepository.save(metadata);
                statusCallback.accept("Upload Completed!");
                isUploading = false;  // Reset upload state after completion

            } catch (Exception e) {
                logger.severe("Upload failed: " + e.getMessage());
                statusCallback.accept("Upload Failed: " + e.getMessage());
                isUploading = false;  // Reset upload state on failure
            }
        });
    }

    public void pauseUpload() {
        if (isUploading && !isPaused) {
            isPaused = true;
            logger.info("Upload paused.");
        }
    }

    public void resumeUpload() {
        if (isUploading && isPaused) {
            isPaused = false;
            logger.info("Upload resumed.");
        }
    }

    public void cancelUpload() {
        if (isUploading) {
            isCancelled = true;
            logger.info("Upload cancelled.");
            // Reset progress and state when cancel is pressed
            resetProgress(null);  // Reset progress
            resetUploadState();  // Reset upload state
        }
    }

    public void cleanupCanceledUpload(File file) {
        if (file != null) {
            // Find the file metadata by its name or ID
            FileMetadata metadata = fileMetadataRepository.findByFileName(file.getName())
                    .orElse(null);

            if (metadata != null) {
                // Delete the file metadata and related chunk metadata
                deleteFileMetadataAndChunks(metadata.getId());
            }
        }
    }


    public void resetUploadState() {
        isUploading = false;  // Reset upload state
        isPaused = false;  // Reset paused state
        isCancelled = false;  // Reset canceled state
        currentFile = null;  // Reset current file
    }

    public void resetProgress(Consumer<Double> progressCallback) {
        // Reset the progress bar to 0 when the upload is canceled or finished
        if (progressCallback != null) {
            progressCallback.accept(0.0);
        }
    }

    public void deleteFileMetadataAndChunks(Long fileId) {
        try {
            // First, delete the chunks associated with the file
            chunkMetadataRepository.deleteByFileId(fileId);

            // Then, delete the file metadata
            fileMetadataRepository.deleteById(fileId);

            logger.info("Deleted file metadata and chunks for fileId: " + fileId);
        } catch (Exception e) {
            logger.severe("Error deleting file metadata and chunks: " + e.getMessage());
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
