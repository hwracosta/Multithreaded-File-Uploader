package com.example.multithreadedfileuploader.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileChunker {
    private static final int CHUNK_SIZE = 1024 * 1024; // 1 MB

    public static void chunkFile(String filePath, String outputDir) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + filePath);
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            int chunkNumber = 0;

            while ((bytesRead = fis.read(buffer)) > 0) {
                File chunkFile = new File(outputDir, "chunk_" + chunkNumber + ".part");
                try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
                    fos.write(buffer, 0, bytesRead);
                }
                chunkNumber++;
            }
        }
        System.out.println("File chunking complete. Chunks saved to: " + outputDir);
    }
}
