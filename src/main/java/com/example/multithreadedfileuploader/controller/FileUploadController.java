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

/**
 * Controller for managing file uploads.
 *
 * <p>This controller handles requests to upload, pause, resume, and cancel file uploads.
 * It interacts with the FileUploadService to perform the actual upload operations
 * and provides endpoints for these functionalities.</p>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li><b>/upload</b>: Handles file uploads.</li>
 *   <li><b>/upload/pause</b>: Pauses an ongoing upload.</li>
 *   <li><b>/upload/resume</b>: Resumes a paused upload.</li>
 *   <li><b>/upload/cancel</b>: Cancels an ongoing upload.</li>
 * </ul>
 */
@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    private final AtomicBoolean isPaused = new AtomicBoolean(false); // Shared state for pause/resume
    private final AtomicBoolean isCancelled = new AtomicBoolean(false); // Shared state for cancel

    /**
     * Constructor-based dependency injection for the FileUploadService.
     *
     * @param fileUploadService The service that handles file uploads.
     */
    @Autowired
    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    /**
     * Endpoint to upload a file.
     *
     * <p>This method converts the uploaded file into a temporary file and starts
     * the upload process through the FileUploadService.</p>
     *
     * @param multipartFile The file uploaded by the client.
     * @return ResponseEntity with a success or error message.
     */
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

    /**
     * Utility method to convert a MultipartFile to a File.
     *
     * <p>The converted file is stored temporarily on the server's filesystem.</p>
     *
     * @param multipartFile The MultipartFile uploaded by the client.
     * @return The converted File object.
     * @throws IOException If an error occurs during file conversion.
     */
    private File convertToFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    /**
     * Endpoint to pause an ongoing upload.
     *
     * @return ResponseEntity with a success message.
     */
    @PostMapping("/pause")
    public ResponseEntity<String> pauseUpload() {
        isPaused.set(true);
        return ResponseEntity.ok("Upload paused successfully.");
    }

    /**
     * Endpoint to resume a paused upload.
     *
     * @return ResponseEntity with a success message.
     */
    @PostMapping("/resume")
    public ResponseEntity<String> resumeUpload() {
        isPaused.set(false);
        return ResponseEntity.ok("Upload resumed successfully.");
    }

    /**
     * Endpoint to cancel an ongoing upload.
     *
     * <p>This method sets the cancellation flag, which interrupts the upload thread
     * and cleans up any ongoing upload process.</p>
     *
     * @return ResponseEntity with a success message.
     */
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelUpload() {
        isCancelled.set(true);
        return ResponseEntity.ok("Upload cancelled successfully.");
    }
}
