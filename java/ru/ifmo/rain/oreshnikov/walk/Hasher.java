package ru.ifmo.rain.oreshnikov.walk;

import java.io.*;

/**
 * @author doreshnikov
 * @date 06-Feb-2019
 */

class Hasher {

    private static final int FNV_PRIME = 0x01000193;
    private static final int FNV_X0 = 0x811c9dc5;

    static int doHash(File file) throws HasherException {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            int hash = FNV_X0;
            int b;
            try {
                while ((b = stream.read()) != -1) {
                    hash = (hash * FNV_PRIME) ^ b;
                }
            } catch (IOException e) {
                throw new HasherException("Can not read bytes from stream: " + e.getMessage());
            }
            return hash;
        } catch (IOException e) {
            throw new HasherException("Can not process file for hashing: " + e.getMessage());
        }
    }

}
