package ru.ifmo.rain.oreshnikov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import ru.ifmo.rain.oreshnikov.student.StudentDB;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author doreshnikov
 * @date 05-Mar-19
 */

public class Implementor implements Impler {

    private final String TAB = "\t";
    private final String SPACE = " ";
    private final String PACKAGE = "package";

    public static void main(String[] args) throws ImplerException {
        new Implementor().implement(StudentDB.class, Path.of("C:/Users/isuca/projects/itmo/4-semester/java-2019/run"));
    }

    private String pack(String... values) {
        return String.join(SPACE, values);
    }

    private String createDefinition(Class<?> token) {
        return "".equals(token.getPackageName()) ?
                "" :
                token.getPackageName() + ";\n\n";
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        try {
            BufferedWriter out = Files.newBufferedWriter(Path.of(root.toString(), token.getSimpleName() + ".java"));
            out.write(createDefinition(token));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
