package com.example.multithreadedfileuploader.service;

import com.example.multithreadedfileuploader.entity.FileMetadata;
import com.example.multithreadedfileuploader.repository.FileMetadataRepository;
import com.example.multithreadedfileuploader.logic.FileChunker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import java.io.File;

@Service
public class FileUploadService {
    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    public void uploadFile(File file, ProgressBar progressBar) {
        new Thread(() -> {
            try {
                // Chunk the file
                String outputDir = "output_chunks";
                FileChunker.chunkFile(file.getAbsolutePath(), outputDir);

                // Save metadata to the database
                FileMetadata metadata = new FileMetadata();
                metadata.setFileName(file.getName());
                metadata.setFileSize(file.length());
                metadata.setTotalChunks(new File(outputDir).listFiles().length);
                metadata.setUploadedChunks(0);
                fileMetadataRepository.save(metadata);

                // Simulate upload progress
                int chunkCount = metadata.getTotalChunks();
                for (int i = 0; i < chunkCount; i++) {
                    Thread.sleep(500); // Simulate upload
                    int uploadedChunks = i + 1;
                    metadata.setUploadedChunks(uploadedChunks);
                    fileMetadataRepository.save(metadata); // Update database
                    double progress = (double) uploadedChunks / chunkCount;
                    Platform.runLater(() -> progressBar.setProgress(progress));
                }

                metadata.setStatus("Completed");
                fileMetadataRepository.save(metadata); // Mark upload as complete
                System.out.println("File upload complete!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
