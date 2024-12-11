package com.example.multithreadedfileuploader.repository;

import com.example.multithreadedfileuploader.entity.ChunkMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ChunkMetadataRepository extends JpaRepository<ChunkMetadata, Long> {

    @Transactional
    @Modifying
    @Query("DELETE FROM ChunkMetadata c WHERE c.fileId = :fileId")
    void deleteByFileId(Long fileId);

    List<ChunkMetadata> findByFileId(Long fileId);

    Optional<ChunkMetadata> findByFileIdAndChunkNumber(Long fileId, int chunkNumber); // Updated to return Optional
}
