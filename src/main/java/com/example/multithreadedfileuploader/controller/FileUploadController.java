package com.example.multithreadedfileuploader.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import com.example.multithreadedfileuploader.service.FileUploadService;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    private final AtomicBoolean isPaused = new AtomicBoolean(false); // Shared state for pause/resume
    private final AtomicBoolean isCancelled = new AtomicBoolean(false); // Shared state for cancel

    // Constructor-based dependency injection
    @Autowired
    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
        try {
            // Convert MultipartFile to File
            File file = convertToFile(multipartFile);

            // Reset pause and cancel states
            isPaused.set(false);
            isCancelled.set(false);

            // Call service to handle the file upload logic
            fileUploadService.uploadFile(file,
                    progress -> System.out.println("Upload progress: " + (progress * 100) + "%"),
                    status -> System.out.println("Upload status: " + status),
                    isPaused::get, // Provide pause state as a supplier
                    isCancelled::get // Provide cancel state as a supplier
            );

            // Return success response
            return ResponseEntity.ok("File upload started successfully.");
        } catch (Exception e) {
            // Handle exceptions and return error response
            e.printStackTrace();
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }

    // Utility method to convert MultipartFile to File
    private File convertToFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    // Endpoint to pause the upload
    @PostMapping("/pause")
    public ResponseEntity<String> pauseUpload() {
        isPaused.set(true);
        return ResponseEntity.ok("Upload paused successfully.");
    }

    // Endpoint to resume the upload
    @PostMapping("/resume")
    public ResponseEntity<String> resumeUpload() {
        isPaused.set(false);
        return ResponseEntity.ok("Upload resumed successfully.");
    }

    // Endpoint to cancel the upload
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelUpload() {
        isCancelled.set(true);
        return ResponseEntity.ok("Upload cancelled successfully.");
    }
}
