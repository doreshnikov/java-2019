package ru.ifmo.rain.oreshnikov.walk;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author doreshnikov
 * @date 07-Feb-2019
 */

public class RecursiveWalk {

    private static final String USAGE_TIP =
            "Expected usage: java RecursiveWalk <input> <output>\nwhere <input> points to a text file with paths list";

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            if (args == null || args.length != 2) {
                System.err.println("Invalid arguments amount");
            } else if (args[0] == null) {
                System.err.println("Input file must be not null");
            } else {
                System.err.println("Output file must be not null");
            }
            System.err.println(USAGE_TIP);
            return;
        }

        try {
            Path inputFilePath = createPath(args[0], "Invalid input file name");
            Path outputFilePath = createPath(args[1], "Invalid output file name");
            new RecursiveWalk().run(inputFilePath, outputFilePath);
        } catch (WalkerException e) {
            System.err.println(e.getMessage());
        }
    }

    private static Path createPath(String fileName, String onFail) throws WalkerException {
        try {
            return Paths.get(fileName);
        } catch (InvalidPathException e) {
            throw new WalkerException(String.format("%s: %s\n", onFail, e.getMessage()));
        }
    }

    private void run(Path inputFilePath, Path outputFilePath) throws WalkerException {
        try (BufferedReader inputReader = Files.newBufferedReader(inputFilePath)) {
            try (BufferedWriter outputWriter = Files.newBufferedWriter(outputFilePath)) {
                FileVisitorHasher hasher = new FileVisitorHasher(outputWriter);
                String path;

                try {
                    while ((path = inputReader.readLine()) != null) {
                        try {
                            Files.walkFileTree(Paths.get(path), hasher);
                        } catch (InvalidPathException e) {
                            hasher.writeHash(0, path);
                        }
                    }
                } catch (IOException e) {
                    throw new WalkerException("Can not read line from input file: " + e.getMessage());
                }
            } catch (IOException e) {
                throw new WalkerException("Error processing output file: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new WalkerException("Error processing input file: " + e.getMessage());
        }
    }

}
