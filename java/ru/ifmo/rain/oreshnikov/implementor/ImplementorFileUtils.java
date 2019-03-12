package ru.ifmo.rain.oreshnikov.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author doreshnikov
 * @date 12-Mar-19
 */

public class ImplementorFileUtils {

    private Path tempDirectory;

    private class FileDeleter extends SimpleFileVisitor<Path> {
        FileDeleter() {
            super();
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }

    ImplementorFileUtils(Path root) throws ImplerException {
        try {
            tempDirectory = Files.createTempDirectory(root.toAbsolutePath(), "tmp");
        } catch (NullPointerException | IOException e) {
            throw new ImplerException("Unable to create temporary directory");
        }
    }

    public static void createDirectoriesTo(Path path) throws ImplerException {
        if (path.getParent() != null) {
            try {
                Files.createDirectories(path.getParent());
            } catch (IOException e) {
                throw new ImplerException(e.getMessage());
            }
        }
    }

    public Path getTempDirectory() {
        return tempDirectory;
    }

    public void cleanTempDirectory() throws ImplerException {
        try {
            Files.walkFileTree(tempDirectory, new FileDeleter());
        } catch (IOException e) {
            throw new ImplerException("Can not delete temp dir");
        }
    }

}
