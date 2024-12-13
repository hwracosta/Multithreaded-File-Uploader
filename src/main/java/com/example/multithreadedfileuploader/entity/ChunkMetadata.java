package com.example.multithreadedfileuploader.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "chunk_metadata")
public class ChunkMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false)
    private Long fileId; // Foreign key to FileMetadata table

    @Column(name = "chunk_number", nullable = false)
    @NotNull
    private Integer chunkNumber;

    @Column(name = "status", nullable = false)
    @NotNull
    private String status = "Pending"; // Default status

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "progress", nullable = false)
    private Double progress = 0.0; // Default progress is 0.0%

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Default constructor
    public ChunkMetadata() {}

    // Parameterized constructor
    public ChunkMetadata(Long fileId, Integer chunkNumber, String status, Double progress) {
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
        this.status = status;
        this.progress = progress;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Integer getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(Integer chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    @PreUpdate
    public void updateTimestamps() {
        updatedAt = LocalDateTime.now();
    }

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
