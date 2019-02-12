package ru.ifmo.rain.oreshnikov.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author doreshnikov
 * @date 07-Feb-19
 */

abstract class Walker {
    protected void run(String inputFilePath, String outputFilePath) throws WalkerException {
        try (BufferedReader inputReader =
                     new BufferedReader(new FileReader(inputFilePath, StandardCharsets.UTF_8))) {
            try (BufferedWriter outputWriter =
                         new BufferedWriter(new FileWriter(outputFilePath, StandardCharsets.UTF_8))) {
                String path;
                try {
                    while ((path = inputReader.readLine()) != null) {
                        doHash(path, outputWriter);
                    }
                } catch (IOException e) {
                    throw new WalkerException("Can not read file paths from input file: " + e.getMessage());
                }
            } catch (IOException e) {
                throw new WalkerException("Can not process file to write to: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new WalkerException("Can not process file to read from: " + e.getMessage());
        }
    }

    protected abstract void doHash(String path, Writer writer) throws WalkerException;

}
