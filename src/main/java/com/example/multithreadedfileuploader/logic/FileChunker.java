package com.example.multithreadedfileuploader.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The FileChunker class provides functionality to split a large file into smaller chunks.
 *
 * <h2>Purpose</h2>
 * <p>
 * This class is responsible for breaking down a large file into smaller parts (chunks),
 * which can be processed and uploaded independently. This approach is particularly useful
 * in large file uploads or in systems that require parallel processing of file data.
 * </p>
 *
 * <h2>Usage</h2>
 * <ul>
 *     <li>Accepts a file path and an output directory as input.</li>
 *     <li>Splits the file into smaller chunks of a fixed size (1 MB by default).</li>
 *     <li>Returns a list of the generated chunk files.</li>
 *     <li>Ensures that chunk files are created in the specified output directory.</li>
 * </ul>
 *
 * <h2>Key Features</h2>
 * <ul>
 *     <li>Handles non-existing files gracefully by throwing an exception.</li>
 *     <li>Ensures output directory is created if it does not exist.</li>
 *     <li>Uses efficient file input/output streams to process the file.</li>
 * </ul>
 *
 * <h2>Important Constants</h2>
 * <ul>
 *     <li><b>CHUNK_SIZE:</b> The size of each chunk in bytes (1 MB by default).</li>
 * </ul>
 *
 * <h2>Important Method</h2>
 * <ul>
 *     <li>
 *         <b>chunkFile(String filePath, String outputDir):</b>
 *         Splits a file into chunks and saves them in the specified output directory.
 *     </li>
 * </ul>
 */
public class FileChunker {
    private static final int CHUNK_SIZE = 1024 * 1024; // 1 MB

    /**
     * Splits a file into smaller chunks and saves them in the specified output directory.
     *
     * @param filePath  The path of the file to be chunked.
     * @param outputDir The directory where the chunks will be saved.
     * @return A list of File objects representing the generated chunks.
     * @throws IOException              If an I/O error occurs during file processing.
     * @throws IllegalArgumentException If the specified file does not exist.
     */
    public static List<File> chunkFile(String filePath, String outputDir) throws IOException {
        // Validate the input file
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        // Ensure the output directory exists
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        List<File> chunks = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            int chunkNumber = 0;

            // Read the file in chunks
            while ((bytesRead = fis.read(buffer)) > 0) {
                File chunkFile = new File(outputDirectory, "chunk_" + chunkNumber + ".part");
                try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
                    fos.write(buffer, 0, bytesRead);
                }
                chunks.add(chunkFile);
                chunkNumber++;
            }
        }

        // Notify the user that the chunking process is complete
        System.out.println("File chunking complete. Chunks saved to: " + outputDir);
        return chunks;
    }
}
