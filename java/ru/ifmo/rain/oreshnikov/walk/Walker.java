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

    protected static Pair<List<String>, OutputStreamWriter> parseArguments(String inputFile, String outputFile) {
        OutputStreamWriter writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(outputFile), Charset.forName("utf-8"));
        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
            return null;
        }

        List<String> paths;
        try {
            paths = Files.readAllLines(Path.of(inputFile), Charset.forName("utf-8"));
        } catch (InvalidPathException | IOException e) {
            System.err.println(e.toString());
            return null;
        }

        return new Pair<>(paths, writer);
    }

    protected void run(List<String> paths, OutputStreamWriter writer) {
        for (String path : paths) {
            doHash(path, writer);
        }
        try {
            writer.close();
        } catch (IOException e) {
            System.err.println("Something went wrong while closing the file");
        }
    }

    protected abstract void doHash(String path, OutputStreamWriter stream);

}
