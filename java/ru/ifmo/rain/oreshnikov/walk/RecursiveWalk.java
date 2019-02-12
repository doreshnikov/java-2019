package ru.ifmo.rain.oreshnikov.walk;

import java.io.File;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Paths;

/**
 * @author doreshnikov
 * @date 07-Feb-2019
 */

public class RecursiveWalk extends Walker {

    private static final String USAGE_TIP =
            "Expected usage: java RecursiveWalk <input> <output>\nwhere <input> points to a text file with paths list";
    private static final Walk WALK =
            new Walk();

    public static void main(String[] args) {
        if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
            System.out.println("Invalid arguments number");
            System.out.println(USAGE_TIP);
            return;
        }
        try {
            new RecursiveWalk().run(args[0], args[1]);
        } catch (WalkerException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void doHash(String dirPath, Writer writer) throws WalkerException {
        if (dirPath == null || writer == null) {
            throw new WalkerException("Expected not-null arguments");
        }
        File dir = new File(dirPath);

        String[] subPathList = new String[0];
        if (dir.isDirectory() && (subPathList = dir.list()) != null) {
            for (String path : subPathList) {
                doHash(Paths.get(dir.getPath(), path).toString(), writer);
            }
        } else {
            if (subPathList == null) {
                System.err.println("Something went wrong while iterating over directory");
            }
            WALK.doHash(dirPath, writer);
        }
    }

}
