package ru.ifmo.rain.oreshnikov.walk;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author doreshnikov
 * @date 06-Feb-2019
 */

class FileVisitorHasher extends SimpleFileVisitor<Path> {

    private static final int FNV_PRIME = 0x01000193;
    private static final int FNV_X0 = 0x811c9dc5;

    BufferedWriter resultWriter;

    FileVisitorHasher(BufferedWriter writer) {
        resultWriter = writer;
    }

    public FileVisitResult writeHash(int hash, String fileName) {
        try {
            resultWriter.write(String.format("%08x %s\n", hash, fileName));
            return FileVisitResult.CONTINUE;
        } catch (IOException e) {
            System.err.printf("Error writing to result file: %s\n", e.getMessage());
            return FileVisitResult.TERMINATE;
        }
    }

    private FileVisitResult doHash(Path file) {
        int hash = FNV_X0;
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
            try {
                for (int b = stream.read(); b != -1; b = stream.read()) {
                    hash = (hash * FNV_PRIME) ^ b;
                }
            } catch (IOException e) {
                System.err.printf("File reading failed: %s\n", e.getMessage());
            }
        } catch (IOException | InvalidPathException e) {
            System.err.printf("File could not be opened for reading: %s\n", e.getMessage());
            hash = 0;
        }
        return writeHash(hash, file.toString());
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        return doHash(file);
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return writeHash(0, file.toString());
    }

}
