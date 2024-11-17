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

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

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

            // Call service to handle the file upload logic
            fileUploadService.uploadFile(file, progress -> {
                // Log progress or perform actions (optional)
                System.out.println("Upload progress: " + (progress * 100) + "%");
            }, status -> {
                // Log status or perform actions (optional)
                System.out.println("Upload status: " + status);
            });

            // Return success response
            return ResponseEntity.ok("File uploaded successfully.");
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
}
