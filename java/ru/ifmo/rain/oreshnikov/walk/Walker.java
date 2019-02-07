package ru.ifmo.rain.oreshnikov.walk;

import net.java.quickcheck.collection.Pair;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author doreshnikov
 * @date 07-Feb-19
 */

abstract class Walker {

    protected static Pair<List<String>, PrintStream> parseArguments(String inputFile, String outputFile) {
        List<String> paths;
        try {
            paths = Files.readAllLines(Path.of(inputFile), Charset.forName("utf-8"));
        } catch (IOException e) {
            System.err.println(e.toString());
            return null;
        }

        PrintStream printStream;
        try {
            printStream = new PrintStream(new FileOutputStream(outputFile));
        } catch (FileNotFoundException e) {
            System.err.println(e.toString());
            return null;
        }

        return new Pair<>(paths, printStream);
    }

    protected void run(List<String> paths, PrintStream stream) {
        for (String path : paths) {
            doHash(new File(path), stream);
        }
    }

    protected abstract void doHash(File path, PrintStream stream);

}
