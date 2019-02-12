package ru.ifmo.rain.oreshnikov.walk;

import net.java.quickcheck.collection.Pair;

import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.util.List;

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

        Pair<List<String>, OutputStreamWriter> arguments = Walker.parseArguments(args[0], args[1]);
        RecursiveWalk recursiveWalk = new RecursiveWalk();
        if (arguments != null) {
            recursiveWalk.run(arguments.getFirst(), arguments.getSecond());
        }
    }

    @Override
    public void doHash(String dirPath, OutputStreamWriter writer) {
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
