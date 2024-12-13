package com.example.multithreadedfileuploader.repository;

import com.example.multithreadedfileuploader.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The FileMetadataRepository interface provides methods for interacting with the `FileMetadata` table in the database.
 *
 * <h2>Purpose</h2>
 * <p>
 * This repository is responsible for performing CRUD operations on the `FileMetadata` entity, which stores metadata
 * about files being uploaded. It extends the {@link JpaRepository}, which provides generic JPA functionality,
 * and defines custom methods specific to file metadata management.
 * </p>
 *
 * <h2>Usage</h2>
 * <ul>
 *     <li>Retrieve file metadata by file name.</li>
 *     <li>Perform standard CRUD operations on file metadata records.</li>
 * </ul>
 *
 * <h2>Important Methods</h2>
 * <ul>
 *     <li><b>findByFileName(String fileName):</b> Finds a specific file metadata record using the file name.</li>
 * </ul>
 *
 * <h2>Annotations</h2>
 * <ul>
 *     <li><b>@Repository:</b> Marks this interface as a Spring Data Repository, making it eligible for Spring's component scanning.</li>
 * </ul>
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    /**
     * Finds a specific file metadata record using the file name.
     *
     * @param fileName The name of the file whose metadata is to be retrieved.
     * @return An {@link Optional} containing the found {@link FileMetadata}, or empty if no record matches.
     */
    Optional<FileMetadata> findByFileName(String fileName);
}
