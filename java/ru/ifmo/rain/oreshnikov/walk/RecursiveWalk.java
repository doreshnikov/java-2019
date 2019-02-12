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
            "Usage: java RecursiveWalk <input> <output>\nwhere <input> points to a text file with paths list";
    private static final Walk WALK =
            new Walk();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid arguments amount");
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
    public void doHash(String dirPath, OutputStreamWriter writer) throws WalkerException {
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
