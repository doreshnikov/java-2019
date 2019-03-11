package ru.ifmo.rain.oreshnikov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.List;
import java.util.NavigableSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author doreshnikov
 * @date 05-Mar-19
 */

public class Implementor implements Impler {

    private static final String TAB = "\t";
    private static final String SPACE = " ";
    private static final String EOLN = System.lineSeparator();
    private static final String ENDL = ";";

    private static final String DEF_OPEN = "{";
    private static final String DEF_CLOSE = "}";
    private static final String ARG_OPEN = "(";
    private static final String ARG_CLOSE = ")";
    private static final String ITEM_SEP = ", ";

    public static void main(String[] args) throws ImplerException {
        new Implementor().implement(NavigableSet.class, Path.of("C:/Users/isuca/projects/itmo/4-semester/java-2019/run"));
    }

    Implementor() {
    }

    private class IndexSupplier implements Supplier<Integer> {
        private int value;

        IndexSupplier() {
            value = 0;
        }

        @Override
        public Integer get() {
            return value++;
        }
    }

    private String doTabs(int cnt) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cnt; i++) {
            builder.append(TAB);
        }
        return builder.toString();
    }

    private <T> String pack(String delimiter, T[] elements, Function<T, String> transform) {
        return Arrays.stream(elements).map(transform).collect(Collectors.joining(delimiter));
    }

    private <T> String packItems(T[] items, Function<T, String> transform) {
        return pack(ITEM_SEP, items, transform);
    }

    private String packParts(String... parts) {
        return Arrays.stream(parts).filter(s -> !"".equals(s)).collect(Collectors.joining(SPACE));
    }

    private String packBlocks(String... blocks) {
        return Arrays.stream(blocks).filter(s -> !"".equals(s)).collect(Collectors.joining(EOLN));
    }

    private String packWideBlocks(String... blocks) {
        return Arrays.stream(blocks).filter(s -> !"".equals(s)).collect(Collectors.joining(EOLN + EOLN));
    }

    private String getIfNotEmpty(String prefix, String itemList) {
        if (!"".equals(itemList)) {
            return packParts(prefix, itemList);
        }
        return "";
    }

    private String getModifiers(int mod) {
        return Modifier.toString(mod & ~Modifier.ABSTRACT & ~Modifier.NATIVE);
    }

    private String getClassModifiers(Class<?> token) {
        return getModifiers(token.getModifiers());
    }

    private String getExecutableModifiers(Executable executable) {
        return getModifiers(executable.getModifiers());
    }

    private String getPackage(Class<?> token) {
        return getIfNotEmpty("package", token.getPackageName()) + ENDL;
    }

    private String getClassName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    private String getExtension(Class<?> token) {
        return packParts(token.isInterface() ? "implements" : "extends", token.getSimpleName());
    }

    private String getClassDefinition(Class<?> token) {
        return packParts(
                getClassModifiers(token),
                "class", getClassName(token),
                getExtension(token)
        );
    }

    private String getExecutableArguments(Executable executable) {
        IndexSupplier argNames = new IndexSupplier();
        return ARG_OPEN +
                packItems(executable.getParameterTypes(), c -> packParts(c.getSimpleName(), "_" + argNames.get())) +
                ARG_CLOSE;
    }

    private String getExecutableArgumentsNames(Executable executable) {
        IndexSupplier argNames = new IndexSupplier();
        return ARG_OPEN +
                packItems(executable.getParameterTypes(), c -> "_" + argNames.get()) +
                ARG_CLOSE;
    }

    private String getExecutableExceptions(Executable executable) {
        return getIfNotEmpty(
                "throws",
                packItems(executable.getExceptionTypes(), Class::getSimpleName)
        );
    }

    private String getConstructorBody(Constructor<?> constructor) {
        return "super" + getExecutableArgumentsNames(constructor) + ENDL;
    }

    private String getDefaultValue(Class<?> ret) {
        if (ret.isPrimitive()) {
            return ret.equals(boolean.class) ? "false" : "0";
        }
        return "null";
    }

    private String getMethodBody(Method method) {
        return packParts("return", getDefaultValue(method.getReturnType())) + ENDL;
    }

    private String getConstructor(Constructor<?> constructor) {
        return doTabs(1) + packParts(
                getExecutableModifiers(constructor),
                getClassName(constructor.getDeclaringClass()) + getExecutableArguments(constructor),
                getExecutableExceptions(constructor),
                packBlocks(
                        DEF_OPEN,
                        doTabs(2) + getConstructorBody(constructor),
                        doTabs(1) + DEF_CLOSE
                )
        );
    }

    private String getMethod(Method method) {
        return doTabs(1) + packParts(
                getExecutableModifiers(method),
                method.getReturnType().getCanonicalName(),
                method.getName() + getExecutableArguments(method),
                getExecutableExceptions(method),
                packBlocks(
                        DEF_OPEN,
                        doTabs(2) + getMethodBody(method),
                        doTabs(1) + DEF_CLOSE
                )
        );
    }

    private String getConstructors(Class<?> token) throws ImplerException {
        List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(c -> !Modifier.isPrivate(c.getModifiers()))
                .collect(Collectors.toList());
        if (constructors.isEmpty()) {
            throw new ImplerException("Target class has no non-private constructors");
        }
        return constructors.stream().map(this::getConstructor).collect(Collectors.joining(EOLN));
    }

    private String getMethods(Class<?> token) {
        return Arrays.stream(token.getDeclaredMethods())
                .filter(m -> Modifier.isAbstract(m.getModifiers()))
                .map(this::getMethod)
                .collect(Collectors.joining(EOLN));
    }

    private String getAllClass(Class<?> token) throws ImplerException {
        return packWideBlocks(
                packParts(getClassDefinition(token), DEF_OPEN),
                getConstructors(token),
                getMethods(token),
                DEF_CLOSE
        );
    }

    private String getFullClass(Class<?> token) throws ImplerException {
        return packWideBlocks(
                getPackage(token),
                getAllClass(token)
        );
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        try {
            BufferedWriter out = Files.newBufferedWriter(Path.of(root.toString(), getClassName(token) + ".java"));
            out.write(getFullClass(token));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
