package ru.ifmo.rain.oreshnikov.walk;

import java.io.*;

/**
 * @author doreshnikov
 * @date 06-Feb-2019
 */

class Utils {

    private static final int FNV_PRIME = 0x01000193;
    private static final int FNV_X0 = 0x811c9dc5;

    static int doHash(File file) throws IOException {
        int hash = FNV_X0;
        InputStream stream = InputStream.nullInputStream();
        try {
            stream = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());;
        }

        int b;
        while ((b = stream.read()) != -1) {
            hash = (hash * FNV_PRIME) ^ b;
        }
        return hash;
    }

}
