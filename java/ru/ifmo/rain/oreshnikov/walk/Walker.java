package ru.ifmo.rain.oreshnikov.walk;

import net.java.quickcheck.collection.Pair;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

/**
 * @author doreshnikov
 * @date 07-Feb-19
 */

abstract class Walker {

    protected void run(String inputFilePath, String outputFilePath) throws WalkerException {
        List<String> paths;
        try {
            paths = Files.readAllLines(Path.of(inputFilePath), Charset.forName("utf-8"));
        } catch (InvalidPathException | IOException e) {
            throw new WalkerException("Can not read given input file: " + e.getMessage());
        }

        OutputStreamWriter writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(outputFilePath), Charset.forName("utf-8"));
        } catch (FileNotFoundException e) {
            throw new WalkerException("Can not open file to write to: " + e.getMessage());
        }

        for (String path : paths) {
            doHash(path, writer);
        }
        try {
            writer.close();
        } catch (IOException e) {
            throw new WalkerException("Can not close output stream: " + e.getMessage());
        }
    }

    protected abstract void doHash(String path, OutputStreamWriter writer) throws WalkerException;

}
