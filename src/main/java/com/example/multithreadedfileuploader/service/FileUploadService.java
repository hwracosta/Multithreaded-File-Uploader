package com.example.multithreadedfileuploader.service;

import com.example.multithreadedfileuploader.entity.FileMetadata;
import com.example.multithreadedfileuploader.repository.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Service
public class FileUploadService {

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // Manage threads efficiently

    public void uploadFile(File file, Consumer<Double> progressCallback, Consumer<String> statusCallback) {
        executorService.submit(() -> {
            try {
                // Calculate total chunks based on file size and chunk size (e.g., 1MB per chunk)
                final long CHUNK_SIZE = 1024 * 1024; // 1 MB
                long totalChunks = (file.length() + CHUNK_SIZE - 1) / CHUNK_SIZE; // Ceiling division

                // Save metadata
                FileMetadata metadata = new FileMetadata();
                metadata.setFileName(file.getName());
                metadata.setFileSize(file.length());
                metadata.setStatus("Uploading");
                metadata.setUploadedChunks(0);
                metadata.setTotalChunks((int) totalChunks);
                fileMetadataRepository.save(metadata);

                // Simulate file upload
                for (int i = 1; i <= totalChunks; i++) {
                    Thread.sleep(100); // Simulate chunk processing

                    // Update progress
                    double progress = (double) i / totalChunks;
                    progressCallback.accept(progress);

                    // Update metadata
                    metadata.setUploadedChunks(i);
                    fileMetadataRepository.save(metadata);
                }

                // Mark as completed
                metadata.setStatus("Completed");
                fileMetadataRepository.save(metadata);

                // Notify success
                statusCallback.accept("Upload Completed!");

            } catch (Exception e) {
                // Log and notify failure
                e.printStackTrace();
                statusCallback.accept("Upload Failed: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        executorService.shutdown(); // Gracefully shut down the thread pool when the application ends
    }
}
