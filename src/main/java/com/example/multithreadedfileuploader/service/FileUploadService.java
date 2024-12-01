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

    // Track paused and canceled states
    private volatile boolean isPaused = false;
    private volatile boolean isCancelled = false;

    public void uploadFile(
            File file,
            Consumer<Double> progressCallback,
            Consumer<String> statusCallback,
            BooleanSupplier pauseCheck,
            BooleanSupplier cancelCheck
    ) {
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

            } catch (Exception e) {
                logger.severe("Upload failed: " + e.getMessage());
                statusCallback.accept("Upload Failed: " + e.getMessage());
            }
        });
    }

    public void fetchProgress(String fileName, Consumer<Double> progressCallback, Consumer<String> statusCallback) {
        FileMetadata metadata = fileMetadataRepository.findByFileName(fileName)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileName));
        progressCallback.accept((double) metadata.getUploadedChunks() / metadata.getTotalChunks());
        statusCallback.accept(metadata.getStatus());
    }

    public synchronized void resumeUpload(File file, Consumer<Double> progressCallback, Consumer<String> statusCallback) {
        if (!isPaused) {
            logger.warning("Resume Upload failed: Upload is not paused.");
            return;
        }
        isPaused = false; // Reset the paused flag
        isCancelled = false; // Reset the canceled flag
        logger.info("Resuming upload for file: " + file.getName());
        uploadFile(file, progressCallback, statusCallback, () -> isPaused, () -> isCancelled);
    }

    public void pauseUpload() {
        isPaused = true;
        logger.info("Upload paused.");
    }

    public void cancelUpload() {
        isCancelled = true;
        logger.info("Upload cancelled.");
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
