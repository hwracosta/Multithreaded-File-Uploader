package com.example.multithreadedfileuploader.repository;

import com.example.multithreadedfileuploader.entity.ChunkMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * The ChunkMetadataRepository interface provides methods for interacting with the `ChunkMetadata` table in the database.
 *
 * <h2>Purpose</h2>
 * <p>
 * This repository is responsible for performing CRUD operations on the `ChunkMetadata` entity, enabling efficient management
 * of file chunks during the upload process. It extends the {@link JpaRepository}, which provides generic JPA functionality,
 * and defines custom methods for chunk-specific operations.
 * </p>
 *
 * <h2>Usage</h2>
 * <ul>
 *     <li>Retrieve all chunks associated with a specific file ID.</li>
 *     <li>Find a specific chunk using the combination of file ID and chunk number.</li>
 *     <li>Delete all chunks associated with a specific file ID.</li>
 * </ul>
 *
 * <h2>Important Methods</h2>
 * <ul>
 *     <li><b>deleteByFileId(Long fileId):</b> Deletes all chunks related to a specific file ID from the database.</li>
 *     <li><b>findByFileId(Long fileId):</b> Retrieves a list of chunks associated with the given file ID.</li>
 *     <li><b>findByFileIdAndChunkNumber(Long fileId, int chunkNumber):</b> Finds a specific chunk based on its file ID and chunk number.</li>
 * </ul>
 *
 * <h2>Annotations</h2>
 * <ul>
 *     <li><b>@Transactional:</b> Ensures the delete operation is managed within a transaction.</li>
 *     <li><b>@Modifying:</b> Indicates that a method modifies the database state.</li>
 *     <li><b>@Query:</b> Defines a custom query for the delete operation.</li>
 * </ul>
 */
public interface ChunkMetadataRepository extends JpaRepository<ChunkMetadata, Long> {

    /**
     * Deletes all chunks associated with a specific file ID.
     *
     * @param fileId The ID of the file whose chunks are to be deleted.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM ChunkMetadata c WHERE c.fileId = :fileId")
    void deleteByFileId(Long fileId);

    /**
     * Retrieves a list of chunks associated with a specific file ID.
     *
     * @param fileId The ID of the file whose chunks are to be retrieved.
     * @return A list of {@link ChunkMetadata} objects.
     */
    List<ChunkMetadata> findByFileId(Long fileId);

    /**
     * Finds a specific chunk using the combination of file ID and chunk number.
     *
     * @param fileId      The ID of the file the chunk belongs to.
     * @param chunkNumber The number of the chunk to find.
     * @return An {@link Optional} containing the found {@link ChunkMetadata}, or empty if no chunk matches.
     */
    Optional<ChunkMetadata> findByFileIdAndChunkNumber(Long fileId, int chunkNumber);
}
