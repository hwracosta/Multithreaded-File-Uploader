package com.example.multithreadedfileuploader.repository;

import com.example.multithreadedfileuploader.entity.ChunkMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChunkMetadataRepository extends JpaRepository<ChunkMetadata, Long> {

    Optional<ChunkMetadata> findByFileIdAndChunkNumber(Long fileId, int chunkNumber);

    List<ChunkMetadata> findByFileId(Long fileId);

    List<ChunkMetadata> findByFileIdAndStatus(Long fileId, String status);
}
