package ru.ifmo.rain.oreshnikov.walk;

import net.java.quickcheck.collection.Pair;

import java.io.*;
import java.util.List;
import java.util.Objects;

/**
 * @author doreshnikov
 * @date 07-Feb-2019
 */

public class RecursiveWalk extends Walker {

    private static final String USAGE_TIP =
            "Usage: java Walk <input> <output>\nwhere <input> points to a text file with paths list";
    private static final Walk WALK =
            new Walk();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid arguments amount");
            System.out.println(USAGE_TIP);
            return;
        }

        Pair<List<String>, PrintStream> arguments = Walker.parseArguments(args[0], args[1]);
        RecursiveWalk recursiveWalk = new RecursiveWalk();
        if (arguments != null) {
            recursiveWalk.run(arguments.getFirst(), arguments.getSecond());
        }
    }

    @Override
    public void doHash(File dir, PrintStream stream) {
        File[] fileList = new File[0];
        if (dir.isDirectory() && (fileList = dir.listFiles()) != null) {
            for (File file : fileList) {
                doHash(file, stream);
            }
        } else {
            if (fileList == null) {
                System.err.println("Something went wrong while iterating over directory");
            }
            WALK.doHash(dir, stream);
        }
    }

}
