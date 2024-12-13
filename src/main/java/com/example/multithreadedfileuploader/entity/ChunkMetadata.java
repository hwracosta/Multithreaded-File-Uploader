package com.example.multithreadedfileuploader.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * The {@code ChunkMetadata} class represents metadata information for individual chunks
 * of a file being uploaded. This entity is used to track the progress and status
 * of file uploads at a granular level, enabling partial uploads, resumable uploads,
 * and concurrent processing of chunks.
 *
 * <p><b>Table:</b> chunk_metadata</p>
 *
 * <h2>Usage</h2>
 * <ul>
 *   <li>Stores metadata for each chunk of an uploaded file.</li>
 *   <li>Tracks chunk-specific progress and upload status.</li>
 *   <li>Linked to the {@code FileMetadata} table through the {@code fileId} foreign key.</li>
 * </ul>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Chunk-level status management (e.g., Pending, Completed).</li>
 *   <li>Granular progress tracking for each chunk.</li>
 *   <li>Timestamps for creation and updates.</li>
 * </ul>
 */
@Entity
@Table(name = "chunk_metadata")
public class ChunkMetadata {

    /**
     * Unique identifier for the chunk metadata.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key referencing the {@code FileMetadata} table.
     * Represents the file to which this chunk belongs.
     */
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    /**
     * Sequential number of the chunk in the file.
     */
    @Column(name = "chunk_number", nullable = false)
    @NotNull
    private Integer chunkNumber;

    /**
     * Current status of the chunk (e.g., "Pending", "Completed").
     */
    @Column(name = "status", nullable = false)
    @NotNull
    private String status = "Pending";

    /**
     * Timestamp when the chunk metadata was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Progress of the chunk upload as a percentage (0.0 to 100.0).
     */
    @Column(name = "progress", nullable = false)
    private Double progress = 0.0;

    /**
     * Timestamp when the chunk metadata was last updated.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Default constructor for JPA.
     */
    public ChunkMetadata() {}

    /**
     * Parameterized constructor to create a {@code ChunkMetadata} instance.
     *
     * @param fileId The ID of the file to which this chunk belongs.
     * @param chunkNumber The sequential number of the chunk.
     * @param status The status of the chunk.
     * @param progress The progress of the chunk upload.
     */
    public ChunkMetadata(Long fileId, Integer chunkNumber, String status, Double progress) {
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
        this.status = status;
        this.progress = progress;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

    /**
     * @return The unique ID of the chunk metadata.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique ID for the chunk metadata.
     * @param id The unique ID to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return The ID of the file to which this chunk belongs.
     */
    public Long getFileId() {
        return fileId;
    }

    /**
     * Sets the file ID for this chunk.
     * @param fileId The file ID to set.
     */
    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    /**
     * @return The sequential number of the chunk.
     */
    public Integer getChunkNumber() {
        return chunkNumber;
    }

    /**
     * Sets the sequential number for the chunk.
     * @param chunkNumber The chunk number to set.
     */
    public void setChunkNumber(Integer chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    /**
     * @return The current status of the chunk.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the chunk.
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The timestamp when the chunk metadata was created.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp for the chunk metadata.
     * @param createdAt The creation timestamp to set.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return The progress of the chunk upload.
     */
    public Double getProgress() {
        return progress;
    }

    /**
     * Sets the progress of the chunk upload.
     * @param progress The progress to set.
     */
    public void setProgress(Double progress) {
        this.progress = progress;
    }

    /**
     * @return The timestamp when the chunk metadata was last updated.
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last updated timestamp for the chunk metadata.
     * @param updatedAt The updated timestamp to set.
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Updates the {@code updatedAt} timestamp before persisting or updating the entity.
     */
    @PrePersist
    @PreUpdate
    public void updateTimestamps() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Converts the chunk metadata object to a string representation.
     *
     * @return A string representation of the chunk metadata.
     */
    @Override
    public String toString() {
        return "ChunkMetadata{" +
                "id=" + id +
                ", fileId=" + fileId +
                ", chunkNumber=" + chunkNumber +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", progress=" + progress +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
