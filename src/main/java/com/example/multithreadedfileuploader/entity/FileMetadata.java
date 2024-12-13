package com.example.multithreadedfileuploader.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Represents metadata for a file being uploaded in the system.
 * <p>
 * This class is an entity managed by JPA and mapped to the "file_metadata" table in the database.
 * It stores information about files being uploaded, such as their name, size, upload status, and the number of chunks processed.
 * </p>
 *
 * <h2>Purpose</h2>
 * <p>
 * This class is used to track and manage the high-level information for file uploads, ensuring the progress and status
 * of each file upload is recorded and accessible.
 * </p>
 *
 * <h2>Usage</h2>
 * <ul>
 *     <li>Tracks file metadata, including name, size, and total/uploaded chunks.</li>
 *     <li>Provides a way to query and update file upload progress through the database.</li>
 *     <li>Works in conjunction with {@link com.example.multithreadedfileuploader.entity.ChunkMetadata} to manage chunk-level data.</li>
 * </ul>
 *
 * <h2>Database Table</h2>
 * <p>
 * This entity maps to the "file_metadata" table in the database. Key columns include:
 * <ul>
 *     <li><b>file_name:</b> Name of the file being uploaded.</li>
 *     <li><b>file_size:</b> Size of the file in bytes.</li>
 *     <li><b>total_chunks:</b> Total number of chunks into which the file is split.</li>
 *     <li><b>uploaded_chunks:</b> Number of chunks successfully uploaded so far.</li>
 *     <li><b>status:</b> Current status of the upload (e.g., "Pending", "Uploading", "Completed").</li>
 *     <li><b>created_at:</b> Timestamp for when the upload process began.</li>
 * </ul>
 * </p>
 *
 * <h2>Important Methods</h2>
 * <ul>
 *     <li><b>Getters and Setters:</b> Used to access and modify the file metadata properties.</li>
 * </ul>
 */
@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    /**
     * The unique identifier for this file metadata record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the file being uploaded.
     */
    @Column(name = "file_name", nullable = false)
    @NotNull
    @Size(max = 255)
    private String fileName;

    /**
     * The size of the file in bytes.
     */
    @Column(name = "file_size", nullable = false)
    @NotNull
    private Long fileSize;

    /**
     * The total number of chunks into which the file is split.
     */
    @Column(name = "total_chunks", nullable = false)
    @NotNull
    private Integer totalChunks;

    /**
     * The number of chunks that have been successfully uploaded so far.
     */
    @Column(name = "uploaded_chunks", nullable = false)
    private Integer uploadedChunks = 0;

    /**
     * The current status of the file upload (e.g., "Pending", "Uploading", "Completed").
     */
    @Column(name = "status", nullable = false)
    private String status = "Pending";

    /**
     * The timestamp for when the upload process began.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Default constructor required by JPA.
     */
    public FileMetadata() {}

    /**
     * Gets the unique identifier for this file metadata record.
     *
     * @return the unique identifier.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier for this file metadata record.
     *
     * @param id the unique identifier.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name of the file being uploaded.
     *
     * @return the file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets the name of the file being uploaded.
     *
     * @param fileName the file name.
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets the size of the file in bytes.
     *
     * @return the file size.
     */
    public Long getFileSize() {
        return fileSize;
    }

    /**
     * Sets the size of the file in bytes.
     *
     * @param fileSize the file size.
     */
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Gets the total number of chunks into which the file is split.
     *
     * @return the total number of chunks.
     */
    public Integer getTotalChunks() {
        return totalChunks;
    }

    /**
     * Sets the total number of chunks into which the file is split.
     *
     * @param totalChunks the total number of chunks.
     */
    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    /**
     * Gets the number of chunks that have been successfully uploaded so far.
     *
     * @return the number of uploaded chunks.
     */
    public Integer getUploadedChunks() {
        return uploadedChunks;
    }

    /**
     * Sets the number of chunks that have been successfully uploaded so far.
     *
     * @param uploadedChunks the number of uploaded chunks.
     */
    public void setUploadedChunks(Integer uploadedChunks) {
        this.uploadedChunks = uploadedChunks;
    }

    /**
     * Gets the current status of the file upload.
     *
     * @return the current upload status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current status of the file upload.
     *
     * @param status the new upload status.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the timestamp for when the upload process began.
     *
     * @return the timestamp for upload creation.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp for when the upload process began.
     *
     * @param createdAt the timestamp for upload creation.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
