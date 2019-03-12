package ru.ifmo.rain.oreshnikov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
        if (args == null || args.length != 2) {
            System.err.println("Invalid arguments number");
        } else if (args[0] == null) {
            System.err.println("Class name should not be empty");
        } else if (args[1] == null) {
            System.err.println("Path should not be empty");
        } else {
            try {
                new Implementor().implement(Class.forName(args[0]), Path.of(args[1]));
            } catch (ClassNotFoundException e) {
                System.err.printf("Invalid class name given: %s\n", e.getMessage());
            } catch (InvalidPathException e) {
                System.err.printf("Invalid path given: %s\n", e.getMessage());
            } catch (ImplerException e) {
                System.err.printf("Error while implementing class: %s\n", e.getMessage());
            }
        }
    }

    public Implementor() {
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

    private class HashableMethod {
        private final Method method;
        private final int PRIME = 239;

        HashableMethod(Method method) {
            this.method = method;
        }

        public Method get() {
            return method;
        }

        @Override
        public int hashCode() {
            return method.getName().hashCode() +
                    PRIME * Arrays.hashCode(method.getParameterTypes()) +
                    PRIME * PRIME * method.getReturnType().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof HashableMethod) {
                HashableMethod hm = (HashableMethod) obj;
                return method.getName().equals(hm.method.getName()) &&
                        Arrays.equals(method.getParameterTypes(), hm.method.getParameterTypes()) &&
                        method.getReturnType().equals(hm.method.getReturnType());
            }
            return false;
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
        return Modifier.toString(mod & ~Modifier.ABSTRACT);
    }

    private String getClassModifiers(Class<?> token) {
        return getModifiers(token.getModifiers() & ~Modifier.INTERFACE);
    }

    private String getExecutableModifiers(Executable executable) {
        return getModifiers(executable.getModifiers() & ~Modifier.NATIVE & ~Modifier.TRANSIENT);
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
                packItems(executable.getParameterTypes(), c -> packParts(c.getCanonicalName(), "_" + argNames.get())) +
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
                packItems(executable.getExceptionTypes(), Class::getCanonicalName)
        );
    }

    private String getConstructorBody(Constructor<?> constructor) {
        return "super" + getExecutableArgumentsNames(constructor) + ENDL;
    }

    private String getDefaultValue(Class<?> ret) {
        if (!ret.isPrimitive()) {
            return "null";
        } else if (ret.equals(void.class)) {
            return "";
        } else if (ret.equals(boolean.class)) {
            return "false";
        } else {
            return "0";
        }
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
        if (token.isInterface()) {
            return "";
        }
        List<Constructor<?>> constructors = Arrays.stream(token.getDeclaredConstructors())
                .filter(c -> !Modifier.isPrivate(c.getModifiers()))
                .collect(Collectors.toList());
        if (constructors.isEmpty()) {
            throw new ImplerException("Class with no non-private constructors can not be extended");
        }
        return constructors.stream()
                .map(this::getConstructor)
                .collect(Collectors.joining(EOLN));
    }

    private void getAbstractMethods(Method[] methods, HashSet<HashableMethod> collector) {
        Arrays.stream(methods)
                .filter(m -> Modifier.isAbstract(m.getModifiers()))
                .map(HashableMethod::new)
                .collect(Collectors.toCollection(() -> collector));
    }

    private String getAbstractMethodsSuperclassesInclusive(Class<?> token) {
        HashSet<HashableMethod> methods = new HashSet<>();
        getAbstractMethods(token.getDeclaredMethods(), methods);
        for (; token != null; token = token.getSuperclass()) {
            getAbstractMethods(token.getMethods(), methods);
        }
        return methods.stream()
                .map(hm -> getMethod(hm.get()))
                .collect(Collectors.joining(EOLN));
    }

    private String getAllClass(Class<?> token) throws ImplerException {
        return packWideBlocks(
                packParts(getClassDefinition(token), DEF_OPEN),
                getConstructors(token),
                getAbstractMethodsSuperclassesInclusive(token),
                DEF_CLOSE
        );
    }

    private String getFullClass(Class<?> token) throws ImplerException {
        return packWideBlocks(
                getPackage(token),
                getAllClass(token)
        );
    }

    private String getFilePath(Class<?> token) {
        return token.getPackageName().replace('.', File.separatorChar);
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Invalid (null) arguments given");
        }
        Path where;
        try {
            where = Path.of(root.toString(), getFilePath(token));
        } catch (InvalidPathException e) {
            throw new ImplerException(e.getMessage());
        }
        try {
            Files.createDirectories(where);
            where = Path.of(where.toString(), getClassName(token) + ".java");
        } catch (IOException e) {
            throw new ImplerException(e.getMessage());
        }
        try (BufferedWriter writer = Files.newBufferedWriter(where)) {
            if (token.isPrimitive() || token.isArray() ||
                    Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
                throw new ImplerException("Unsupported class token given");
            }
            try {
                writer.write(getFullClass(token));
            } catch (IOException e) {
                throw new ImplerException(e.getMessage());
            }
        } catch (IOException e) {
            throw new ImplerException(e.getMessage());
        }
    }

}
