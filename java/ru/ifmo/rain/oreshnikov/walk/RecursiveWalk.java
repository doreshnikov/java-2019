package ru.ifmo.rain.oreshnikov.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
                System.out.println("Invalid arguments amount");
            } else if (args[0] == null) {
                System.out.println("Input file must not be null");
            } else {
                System.out.println("Output file must not be null");
            }
            System.out.println(USAGE_TIP);
            return;
        }
        try {
            new RecursiveWalk().run(args[0], args[1]);
        } catch (WalkerException e) {
            System.err.println(e.getMessage());
        }
    }

    protected void run(String inputFilePath, String outputFilePath) throws WalkerException {
        try (BufferedReader inputReader =
                     new BufferedReader(new FileReader(inputFilePath, StandardCharsets.UTF_8))) {
            try (BufferedWriter outputWriter =
                         new BufferedWriter(new FileWriter(outputFilePath, StandardCharsets.UTF_8))) {
                String path;
                try {
                    while ((path = inputReader.readLine()) != null) {
                        try {
                            Files.walkFileTree(Paths.get(path), new FileVisitorHasher(outputWriter));
                        } catch (IOException | InvalidPathException e) {
                            FileVisitorHasher.writeHash(0, path, outputWriter);
                        }
                    }
                } catch (IOException e) {
                    throw new WalkerException("Can not process input file data: " + e.getMessage());
                }
            } catch (IOException e) {
                throw new WalkerException("Error processing output file: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new WalkerException("Error processing input file: " + e.getMessage());
        }
    }

}
