/**
 * Service class responsible for handling the logic of file uploads.
 * <p>
 * This service provides methods for uploading files in chunks, pausing, resuming,
 * and canceling uploads, and managing metadata in the database. It uses a fixed thread pool
 * to perform file upload operations concurrently and ensures the upload process is tracked
 * and managed efficiently.
 * </p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Supports large file uploads by splitting files into manageable chunks.</li>
 *   <li>Allows pausing, resuming, and canceling uploads with real-time status updates.</li>
 *   <li>Maintains upload progress and metadata in a database.</li>
 *   <li>Provides cleanup mechanisms for canceled uploads.</li>
 * </ul>
 */
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

    /**
     * Uploads a file in chunks and tracks the progress in real-time.
     *
     * @param file The file to upload.
     * @param progressCallback A callback to report the upload progress (0.0 to 1.0).
     * @param statusCallback A callback to report the status of the upload (e.g., "Completed", "Cancelled").
     * @param pauseCheck A supplier to check if the upload should be paused.
     * @param cancelCheck A supplier to check if the upload should be canceled.
     */
    public void uploadFile(
            File file,
            Consumer<Double> progressCallback,
            Consumer<String> statusCallback,
            BooleanSupplier pauseCheck,
            BooleanSupplier cancelCheck
    ) {
        // Reset progress and upload state when a new file is selected
        if (currentFile != null && !currentFile.equals(file)) {
            resetUploadState();
            resetProgress(progressCallback);
        }

        currentFile = file;

        if (isCancelled || !isUploading) {
            resetProgress(progressCallback);
        }

        isUploading = true;

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
                        statusCallback.accept("Upload Cancelled");
                        metadata.setStatus("Cancelled");
                        fileMetadataRepository.save(metadata);
                        resetProgress(progressCallback);
                        isUploading = false;
                        return;
                    }

                    while (pauseCheck.getAsBoolean()) {
                        Thread.sleep(500);
                    }

                    Thread.sleep(100);

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
                isUploading = false;

            } catch (Exception e) {
                logger.severe("Upload failed: " + e.getMessage());
                statusCallback.accept("Upload Failed: " + e.getMessage());
                isUploading = false;
            }
        });
    }

    /**
     * Pauses the ongoing file upload.
     */
    public void pauseUpload() {
        if (isUploading && !isPaused) {
            isPaused = true;
            logger.info("Upload paused.");
        }
    }

    /**
     * Resumes a paused file upload.
     */
    public void resumeUpload() {
        if (isUploading && isPaused) {
            isPaused = false;
            logger.info("Upload resumed.");
        }
    }

    /**
     * Cancels the ongoing file upload and resets the upload state.
     */
    public void cancelUpload() {
        if (isUploading) {
            isCancelled = true;
            logger.info("Upload cancelled.");
            resetProgress(null);
            resetUploadState();
        }
    }

    /**
     * Cleans up metadata for a canceled upload.
     *
     * @param file The file whose metadata should be removed.
     */
    /**
     * Cleans up metadata for a canceled upload by removing all associated database entries.
     *
     * @param file The file whose metadata should be removed.
     */
    public void cleanupCanceledUpload(File file) {
        if (file != null) {
            try {
                // Find the file metadata by file name
                FileMetadata metadata = fileMetadataRepository.findByFileName(file.getName()).orElse(null);

                if (metadata != null) {
                    // Delete associated chunk metadata
                    chunkMetadataRepository.deleteByFileId(metadata.getId());

                    // Delete file metadata
                    fileMetadataRepository.deleteById(metadata.getId());

                    logger.info("Successfully removed file and chunk metadata for file: " + file.getName());
                } else {
                    logger.warning("No metadata found for file: " + file.getName());
                }
            } catch (Exception e) {
                logger.severe("Error during cleanup for file: " + file.getName() + " - " + e.getMessage());
            }
        }
    }


    /**
     * Resets the upload state to its default values.
     */
    public void resetUploadState() {
        isUploading = false;
        isPaused = false;
        isCancelled = false;
        currentFile = null;
    }

    /**
     * Resets the progress callback to 0.
     *
     * @param progressCallback The progress callback to reset.
     */
    public void resetProgress(Consumer<Double> progressCallback) {
        if (progressCallback != null) {
            progressCallback.accept(0.0);
        }
    }

    /**
     * Deletes metadata for a file and its associated chunks.
     *
     * @param fileId The ID of the file whose metadata and chunks should be removed.
     */
    public void deleteFileMetadataAndChunks(Long fileId) {
        try {
            chunkMetadataRepository.deleteByFileId(fileId);
            fileMetadataRepository.deleteById(fileId);
            logger.info("Deleted file metadata and chunks for fileId: " + fileId);
        } catch (Exception e) {
            logger.severe("Error deleting file metadata and chunks: " + e.getMessage());
        }
    }

    /**
     * Shuts down the executor service used for file uploads.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
