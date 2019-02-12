package ru.ifmo.rain.oreshnikov.walk;

import java.io.*;

/**
 * @author doreshnikov
 * @date 06-Feb-2019
 */

public class Walk extends Walker {

    private static final String USAGE_TIP =
            "Expected usage: java Walk <input> <output>\nwhere <input> points to a text file with paths list";
    public static final Walk WALK = new Walk();

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Invalid arguments number");
            System.out.println(USAGE_TIP);
            return;
        }
        try {
            WALK.run(args[0], args[1]);
        } catch (WalkerException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void doHash(String filePath, Writer writer) throws WalkerException {
        if (filePath == null || writer == null) {
            throw new WalkerException("Expected not-null arguments");
        }
        File file = new File(filePath);

        int hash = 0;
        if (file.isFile()) {
            try {
                hash = Hasher.doHash(file);
            } catch (HasherException e) {
                System.err.println(e.toString());
            }
        } else {
            System.err.printf("File %s is missing\n", filePath);
        }

        try {
            writer.append(String.format("%08x ", hash)).append(filePath).append('\n');
        } catch (IOException e) {
            throw new WalkerException("Something went wrong while writing to file: " + e.getMessage());
        }
    }

}
