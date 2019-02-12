package ru.ifmo.rain.oreshnikov.walk;

import net.java.quickcheck.collection.Pair;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

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
    public void doHash(String filePath, OutputStreamWriter writer) {
        File file = new File(filePath);

        int hash = 0;
        if (file.isFile()) {
            try {
                hash = Utils.doHash(file);
            } catch (IOException e) {
                System.err.println(e.toString());
            }
        } else {
            System.err.printf("File %s is missing\n", filePath);
        }

        try {
            writer.append(String.format("%08x ", hash)).append(filePath).append('\n');
        } catch (IOException e) {
            System.err.println("Something went wrong while writing to file");
        }
    }

}
