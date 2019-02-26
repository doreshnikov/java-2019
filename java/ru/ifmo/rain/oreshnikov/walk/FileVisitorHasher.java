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

    static FileVisitResult writeHash(int hash, String fileName, BufferedWriter output) throws IOException {
        output.write(String.format("%08x %s\n", hash, fileName));
        return FileVisitResult.CONTINUE;
    }

    private FileVisitResult doHash(Path file) throws IOException {
        int hash = FNV_X0;
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(file))) {
            int b;
            while ((b = stream.read()) != -1) {
                hash = (hash * FNV_PRIME) ^ b;
            }
        } catch (IOException | InvalidPathException e) {
            System.err.printf("File hashing failed: %s", e.getMessage());
            hash = 0;
        }
        return writeHash(hash, file.toString(), resultWriter);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (attrs.isRegularFile()) {
            return doHash(file);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return writeHash(0, file.toString(), resultWriter);
    }

}
