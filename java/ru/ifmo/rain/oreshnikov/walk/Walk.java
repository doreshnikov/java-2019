package ru.ifmo.rain.oreshnikov.walk;

import net.java.quickcheck.collection.Pair;

import java.io.*;
import java.util.List;

import ru.ifmo.rain.oreshnikov.walk.Utils;

/**
 * @author doreshnikov
 * @date 06-Feb-2019
 */

public class Walk extends Walker {

    private static final String USAGE_TIP =
            "Usage: java Walk <input> <output>\nwhere <input> points to a text file with paths list";

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid arguments amount");
            System.out.println(USAGE_TIP);
            return;
        }

        Pair<List<String>, OutputStreamWriter> arguments = Walker.parseArguments(args[0], args[1]);
        Walk walk = new Walk();
        if (arguments != null) {
            walk.run(arguments.getFirst(), arguments.getSecond());
        }
    }

    @Override
    public void doHash(File file, OutputStreamWriter writer) {
        int hash = 0;
        if (file.isFile()) {
            try {
                hash = Utils.doHash(file);
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        } else {
            System.err.printf("File %s is missing\n", Utils.escape(file.getPath()));
        }

        try {
            writer.write(String.format("%08x %s\n", hash, Utils.escape(file.getPath())));
        } catch (IOException e) {
            System.err.println("Something went wrong while writing to file");
        }
    }

}
