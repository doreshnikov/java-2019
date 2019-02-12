package ru.ifmo.rain.oreshnikov.walk;

import java.io.*;
import java.nio.charset.Charset;

/**
 * @author doreshnikov
 * @date 07-Feb-19
 */

abstract class Walker {

    protected void run(String inputFilePath, String outputFilePath) throws WalkerException {
        BufferedReader inputReader;
        try {
            inputReader = new BufferedReader(new FileReader(inputFilePath, Charset.forName("utf-8")));
        } catch (IOException e) {
            throw new WalkerException("Can not open file to read from: " + e.getMessage());
        }

        OutputStreamWriter outputWriter;
        try {
            outputWriter = new OutputStreamWriter(new FileOutputStream(outputFilePath), Charset.forName("utf-8"));
        } catch (FileNotFoundException e) {
            throw new WalkerException("Can not open file to write to: " + e.getMessage());
        }

        String path;
        try {
            while ((path = inputReader.readLine()) != null) {
                doHash(path, outputWriter);
            }
        } catch (IOException e) {
            throw new WalkerException("Can not read file paths from input file: " + e.getMessage());
        }
        try {
            outputWriter.close();
        } catch (IOException e) {
            throw new WalkerException("Can not close output stream: " + e.getMessage());
        }
    }

    protected abstract void doHash(String path, OutputStreamWriter writer) throws WalkerException;

}
