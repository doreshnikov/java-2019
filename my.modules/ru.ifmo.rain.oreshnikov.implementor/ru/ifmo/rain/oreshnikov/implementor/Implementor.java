package ru.ifmo.rain.oreshnikov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
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
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Class implementing {@link Impler} and {@link JarImpler}. Provides public methods to implement <code>.java</code>
 * and <code>.jar</code> files for classes extending or implementing given class of interface.
 *
 * @author doreshnikov
 * @version 1.0
 */
public class Implementor implements Impler, JarImpler {

    /**
     * Tabulation-type indentation for generated <code>.java</code> files.
     */
    private static final String TAB = "\t";
    /**
     * Space-type indentation for generated <code>.java</code> files.
     */
    private static final String SPACE = " ";
    /**
     * Line separator for generated <code>.java</code> files.
     */
    private static final String LINE_SEP = System.lineSeparator();

    /**
     * Definition opening sign for generated <code>.java</code> files.
     */
    private static final String DEF_OPEN = "{";
    /**
     * Definition closing sign for generated <code>.java</code> files.
     */
    private static final String DEF_CLOSE = "}";
    /**
     * Argument list opening sign for generated <code>.java</code> files.
     */
    private static final String ARG_OPEN = "(";
    /**
     * Argument list closing sign for generated <code>.java</code> files.
     */
    private static final String ARG_CLOSE = ")";
    /**
     * Argument separator for generated <code>.java</code> files.
     */
    private static final String ARG_SEP = ", ";

    /**
     * Main function. Provides console interface for {@link Implementor}.
     * Runs in two modes depending on {@code args}:
     * <ol>
     * <li>2-argument <code>className outputPath</code> creates <code>.java</code> file by executing
     * provided with {@link Impler} method {@link #implement(Class, Path)}</li>
     * <li>3-argument <code>-jar className jarOutputPath</code> creates <code>.jar</code> file by executing
     * provided with {@link JarImpler} method {@link #implementJar(Class, Path)}</li>
     * </ol>
     * All arguments must be correct and not-null. If some arguments are incorrect
     * or an error occurs in runtime an information message is printed and implementation is aborted.
     *
     * @param args command line arguments for application
     */
    public static void main(String[] args) {
        if (args == null || args.length < 2 || args.length > 3) {
            System.err.println("Invalid arguments number, expected [-jar] <class.name> <output.path>");
        } else {
            for (String arg : args) {
                if (arg == null) {
                    System.err.println("All arguments should be not null");
                    return;
                }
            }
            try {
                if (args.length == 2) {
                    new Implementor().implement(Class.forName(args[0]), Path.of(args[1]));
                } else if (!"-jar".equals(args[0])) {
                    System.err.printf("Invalid argument usage: only option available is -jar and %s given", args[0]);
                } else {
                    new Implementor().implementJar(Class.forName(args[1]), Path.of(args[2]));
                }
            } catch (ClassNotFoundException e) {
                System.err.printf("Invalid class name given: %s\n", e.getMessage());
            } catch (InvalidPathException e) {
                System.err.printf("Invalid path given: %s\n", e.getMessage());
            } catch (ImplerException e) {
                System.err.printf("Error while creating %s file: %s\n", args.length == 2 ? "java" : "jar",
                        e.getMessage());
            }
        }
    }

    /**
     * Default constructor. Creates new instance of {@link Implementor}.
     */
    public Implementor() {
    }

    /**
     * Integer values supplier static class. Returns consecutive integer values.
     * Is used for argument names generation in {@link #getExecutableArguments(Executable)}
     * and {@link #getExecutableArgumentNames(Executable)}.
     */
    private static class IndexSupplier implements IntSupplier {
        /**
         * Integer value used for consecutive numbers generation.
         */
        private int value;

        /**
         * Default constructor. Creates new instance of {@link IntSupplier} with {@link #value}
         * set to zero.
         */
        IndexSupplier() {
            value = 0;
        }

        /**
         * Supplier getter method. Returns next integer depending on inner {@link #value}.
         *
         * @return next integer value
         */
        @Override
        public int getAsInt() {
            return value++;
        }
    }

    /**
     * Hash providing method wrapper static class. Provides custom equality check for {@link Method}s
     * independent from {@link Method#getDeclaringClass()}.
     *
     * @see Method#hashCode()
     * @see Method#equals(Object)
     */
    private static class HashableMethod {
        /**
         * Inner wrapped {@link Method} instance.
         */
        private final Method method;
        /**
         * Prime multiplier used in hashing.
         */
        private final int PRIME = 239;
        /**
         * Prime base module used in hashing.
         */
        private final int BASE = 1000000007;

        /**
         * Wrapping constructor. Creates new instance of {@link HashableMethod} with wrapped {@link Method} inside.
         *
         * @param method instance of {@link Method} class to be wrapped inside
         */
        HashableMethod(Method method) {
            this.method = method;
        }

        /**
         * Getter for wrapped instance of {@link Method} class.
         *
         * @return wrapped {@link #method}
         */
        public Method get() {
            return method;
        }

        /**
         * Hash code calculator. Calculates polynomial hash code for wrapped {@link #method}
         * using it's name, parameter types and return type.
         *
         * @return integer hash code value
         */
        @Override
        public int hashCode() {
            return ((method.getName().hashCode() +
                    PRIME * Arrays.hashCode(method.getParameterTypes())) % BASE +
                    (PRIME * PRIME) % BASE * method.getReturnType().hashCode()) % BASE;
        }

        /**
         * Checks if this wrapper is equal to another object.
         * Object is considered equal if it is an instance of {@link HashableMethod}
         * and has a wrapped {@link #method} inside with same name, parameter types and return type.
         *
         * @param obj object to compare with
         * @return <code>true</code> if objects are equal, <code>false</code> otherwise
         */
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

    /**
     * Unicode encoder for resulting <code>.java</code> file.
     * Escapes all unicode characters in <code>\\u</code> notation.
     *
     * @param arg a {@link String} instance to be encoded
     * @return a {@link String} representing unicode-escaped {@code arg}
     */
    private static String encode(String arg) {
        StringBuilder builder = new StringBuilder();
        for (char c : arg.toCharArray()) {
            builder.append(c < 128 ? String.valueOf(c) : String.format("\\u%04x", (int) c));
        }
        return builder.toString();
    }

    /**
     * Tab provider. Returns a string containing an exact number of {@link #TAB} symbols.
     *
     * @param cnt a number of tabs to return
     * @return a {@link String} containing exactly {@code cnt} of tabs
     */
    private String doTabs(int cnt) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cnt; i++) {
            builder.append(TAB);
        }
        return builder.toString();
    }

    /**
     * Custom elements joiner. Filters given elements, than maps them to {@link String}s
     * and concatenates them with given delimiter.
     *
     * @param delimiter delimiter separating given values
     * @param elements  array of values to be joined together
     * @param filter    filtering function
     * @param map       transforming to {@link String} function
     * @param <T>       type of given elements
     * @return a {@link String} containing all transformed filtered {@code elements} separated by {@code delimiter}
     */
    private <T> String pack(String delimiter, T[] elements, Predicate<T> filter, Function<T, String> map) {
        return Arrays.stream(elements).filter(filter).map(map).collect(Collectors.joining(delimiter));
    }

    /**
     * Custom arguments joiner. Joins arguments by {@link #ARG_SEP} using given transformation.
     *
     * @param items     array of arguments representations
     * @param transform transforming to {@link String} function
     * @param <T>       type of given items
     * @return a {@link String} containing all transformed {@code items} separated by {@link #ARG_SEP}
     */
    private <T> String packItems(T[] items, Function<T, String> transform) {
        return pack(ARG_SEP, items, x -> true, transform);
    }

    /**
     * Custom {@link String} joiner. Joins varargs of {@link String} by {@link #SPACE}
     * removing all empty arguments first.
     *
     * @param parts strings to be joined
     * @return a {@link String} containing all non-empty {@code parts} separated by {@link #SPACE}
     */
    private String packParts(String... parts) {
        return pack(SPACE, parts, s -> !"".equals(s), Function.identity());
    }

    /**
     * Custom code blocks joiner. Joins statements or methods {@link String} representations by {@link #LINE_SEP}
     * removing all empty blocks first.
     *
     * @param wide   flag showing if the double line separator is needed
     * @param blocks array of code blocks representations
     * @return a {@link String} containing all non-empty {@code blocks} separated by needed number of {@link #LINE_SEP}s
     */
    private String packBlocks(boolean wide, String... blocks) {
        return pack(wide ? LINE_SEP + LINE_SEP : LINE_SEP, blocks, s -> !"".equals(s), Function.identity());
    }

    /**
     * Custom two {@link String}s joiner. Returns their combination using {@link #packParts(String...)}
     * if {@code item} is not empty and an empty string otherwise.
     *
     * @param prefix a prefix for concatenation
     * @param item   a string indicating should both parts be used or not
     * @return an empty {@link String} if the {@code item} is empty
     * and concatenation of {@code prefix} and {@code item} with {@link #SPACE} between them otherwise
     */
    private String packIfNotEmpty(String prefix, String item) {
        if (!"".equals(item)) {
            return packParts(prefix, item);
        }
        return "";
    }

    /**
     * Returns a representation for {@link Class}, {@link Method} or {@link Constructor} common modifiers.
     * {@link Modifier#ABSTRACT} is not included.
     *
     * @param mod integer representing modifiers mask
     * @return a {@link String} representing all non-abstract modifiers if present
     * @see Method#getModifiers()
     * @see Class#getModifiers()
     * @see Modifier#toString(int)
     */
    private String getModifiers(int mod) {
        return Modifier.toString(mod & ~Modifier.ABSTRACT);
    }

    /**
     * Returns a representation of given {@link Class} modifiers.
     * {@link Modifier#ABSTRACT} and {@link Modifier#INTERFACE} are not included.
     *
     * @param token instance of given class {@link Class} object
     * @return a {@link String} representing all non-abstract and non-interface modifiers of given {@code token}
     */
    private String getClassModifiers(Class<?> token) {
        return getModifiers(token.getModifiers() & ~Modifier.INTERFACE);
    }

    /**
     * Returns a representation of given {@link Method} of {@link Constructor} modifiers/
     * {@link Modifier#ABSTRACT}, {@link Modifier#NATIVE} and {@link Modifier#TRANSIENT} are not included.
     *
     * @param executable an instance of {@link Executable} which is a method or a constructor
     * @return a {@link String} representing all neither abstract, native or transient modifiers of given {@code executable}
     */
    private String getExecutableModifiers(Executable executable) {
        return getModifiers(executable.getModifiers() & ~Modifier.NATIVE & ~Modifier.TRANSIENT);
    }

    /**
     * Returns a package declaration for implemented <code>.java</code> class file.
     * Uses {@link #packIfNotEmpty(String, String)} to determine if this declaration is needed.
     *
     * @param token instance of given class {@link Class} object
     * @return a {@link String} containing new class package declaration
     */
    private String getPackage(Class<?> token) {
        return packIfNotEmpty("package", token.getPackageName()) + ";";
    }

    /**
     * Returns a class name for implemented <code>.java</code> class.
     *
     * @param token instance of given class {@link Class} object
     * @return a {@link String} containing new class name
     */
    private String getClassName(Class<?> token) {
        return token.getSimpleName() + "Impl";
    }

    /**
     * Returns <code>extends</code> or <code>implements</code> declaration of new class
     * depending on given base class {@code token} using {@link #packParts(String...)}.
     *
     * @param token instance of given class {@link Class} object
     * @return a {@link String} containing new class superclass declaration
     */
    private String getExtension(Class<?> token) {
        return packParts(token.isInterface() ? "implements" : "extends", token.getCanonicalName());
    }

    /**
     * Returns a representation of new class full declaration line containing modifiers, class name and superclass
     * using {@link #packParts(String...)}.
     *
     * @param token instance of given class {@link Class} object
     * @return a {@link String} containing new class declaration
     */
    private String getClassDefinition(Class<?> token) {
        return packParts(
                getClassModifiers(token),
                "class", getClassName(token),
                getExtension(token)
        );
    }

    /**
     * Returns a representation of {@link Executable} argument list with types and names
     * using {@link #packItems(Object[], Function)}.
     * Argument names are generated by {@link IndexSupplier}.
     *
     * @param executable an instance of {@link Executable} which is a method or a constructor
     * @return a {@link String} representing this {@code executable} argument list
     */
    private String getExecutableArguments(Executable executable) {
        IndexSupplier argNames = new IndexSupplier();
        return ARG_OPEN +
                packItems(executable.getParameterTypes(), c -> packParts(c.getCanonicalName(),
                        "_" + argNames.getAsInt())) +
                ARG_CLOSE;
    }

    /**
     * Returns a representation of {@link Executable} argument names list
     * using {@link #packItems(Object[], Function)}.
     * Argument names are generated by {@link IndexSupplier}.
     *
     * @param executable an instance of {@link Executable} which is a constructor
     * @return a {@link String} representing this {@code executable} argument names list
     */
    private String getExecutableArgumentNames(Executable executable) {
        IndexSupplier argNames = new IndexSupplier();
        return ARG_OPEN +
                packItems(executable.getParameterTypes(), c -> "_" + argNames.getAsInt()) +
                ARG_CLOSE;
    }

    /**
     * Returns a representation of {@link Executable} exceptions list using {@link #packIfNotEmpty(String, String)}.
     *
     * @param executable an instance of {@link Executable} which is a method or a constructor
     * @return a {@link String} representing all throws from this {@code executable}
     */
    private String getExecutableExceptions(Executable executable) {
        return packIfNotEmpty(
                "throws",
                packItems(executable.getExceptionTypes(), Class::getCanonicalName)
        );
    }

    /**
     * Returns a representation of super constructor call in new class constructor body.
     *
     * @param constructor an instance of {@link Constructor}
     * @return a {@link String} representation of new class constructor body
     */
    private String getConstructorBody(Constructor<?> constructor) {
        return "super" + getExecutableArgumentNames(constructor) + ";";
    }

    /**
     * Returns default return value representation for method with given {@link Method#getReturnType()}.
     *
     * @param ret some method return value type
     * @return a {@link String} representing default return value of this type
     */
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

    /**
     * Returns a representation of simple default return value in new class method body.
     * If given method has no default return value, return statement is skipped using {@link #packParts(String...)}.
     *
     * @param method an instance of {@link Method}
     * @return a {@link String} representation of new class method body
     */
    private String getMethodBody(Method method) {
        return packParts("return", getDefaultValue(method.getReturnType())) + ";";
    }

    /**
     * Full constructor builder. Returns new class full constructor representation using {@link #packParts(String...)}
     * to combine together {@link #getExecutableModifiers(Executable)}, {@link #getClassName(Class)},
     * {@link #getExecutableExceptions(Executable)} and {@link #packBlocks(boolean, String...)} used for body.
     *
     * @param constructor an instance of {@link Constructor}
     * @return a {@link String} representation of constructor declaration and body
     */
    private String getConstructor(Constructor<?> constructor) {
        return doTabs(1) + packParts(
                getExecutableModifiers(constructor),
                getClassName(constructor.getDeclaringClass()) + getExecutableArguments(constructor),
                getExecutableExceptions(constructor),
                packBlocks(false,
                        DEF_OPEN,
                        doTabs(2) + getConstructorBody(constructor),
                        doTabs(1) + DEF_CLOSE
                )
        );
    }

    /**
     * Full method builder. Returns new class full method representation using {@link #packParts(String...)}
     * to combine together {@link #getExecutableModifiers(Executable)}, method return and name data,
     * {@link #getExecutableArguments(Executable)}, {@link #getExecutableExceptions(Executable)}
     * and {@link #packBlocks(boolean, String...)} used for body.
     *
     * @param method an instance of {@link Method}
     * @return a {@link String} representation of method declaration and body
     */
    private String getMethod(Method method) {
        return doTabs(1) + packParts(
                getExecutableModifiers(method),
                method.getReturnType().getCanonicalName(),
                method.getName() + getExecutableArguments(method),
                getExecutableExceptions(method),
                packBlocks(false,
                        DEF_OPEN,
                        doTabs(2) + getMethodBody(method),
                        doTabs(1) + DEF_CLOSE
                )
        );
    }

    /**
     * All constructors builder.
     * Returns new class all constructor representations mentioned in {@link #getConstructor(Constructor)}.
     *
     * @param token type token to create implementation for
     * @return a {@link String} representation of all constructors in the class separated by {@link #LINE_SEP}
     * @throws ImplerException if there are no non-private constructors available for overriding
     */
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
                .collect(Collectors.joining(LINE_SEP));
    }

    /**
     * Abstract methods collector. Collects all abstract methods from given {@code methods} to given {@code collector}.
     *
     * @param methods an array of {@link Method}s to select from
     * @param collector a {@link HashSet} of {@link HashableMethod} to collect to
     */
    private void collectAbstractMethods(Method[] methods, HashSet<HashableMethod> collector) {
        Arrays.stream(methods)
                .filter(m -> Modifier.isAbstract(m.getModifiers()))
                .map(HashableMethod::new)
                .forEach(collector::add);
    }

    // TODO: add unimplemented interfaces methods collection
    /**
     * All methods builder.
     * Returns new class all abstract method representations mentioned in {@link #getMethod(Method)}.
     * Collects all superclasses' abstract methods using {@link #collectAbstractMethods(Method[], HashSet)}.
     *
     * @param token type token to create implementation for
     * @return a {@link String} representation of all superclasses' abstract methods separated by {@link #LINE_SEP}
     */
    private String getAbstractMethodsSuperclassesInclusive(Class<?> token) {
        HashSet<HashableMethod> methods = new HashSet<>();
        collectAbstractMethods(token.getDeclaredMethods(), methods);
        for (; token != null; token = token.getSuperclass()) {
            collectAbstractMethods(token.getMethods(), methods);
        }
        return methods.stream()
                .map(hm -> getMethod(hm.get()))
                .collect(Collectors.joining(LINE_SEP));
    }

    /**
     * All class builder.
     * Returns a representation of new class declaration with full body definition
     * containing all constructors and methods built with {@link #packBlocks(boolean, String...)}.
     *
     * @param token type token to create implementation for
     * @return a {@link String} representation of all new class declarations and definitions
     * @throws ImplerException if there are no non-private constructors to override in original class {@code token}
     */
    private String getAllClass(Class<?> token) throws ImplerException {
        return packBlocks(true,
                packParts(getClassDefinition(token), DEF_OPEN),
                getConstructors(token),
                getAbstractMethodsSuperclassesInclusive(token),
                DEF_CLOSE
        );
    }

    /**
     * Full <code>.java</code> file builder.
     * Returns full <code>.java</code> class file content containing package declaration and full class definition.
     *
     * @param token type token to create implementation for
     * @return a {@link String} representing whole <code>.java</code> file for a newly implemented class
     * @throws ImplerException if there are no non-private constructors to override in original class {@code token}
     */
    private String getFullClass(Class<?> token) throws ImplerException {
        return packBlocks(true,
                getPackage(token),
                getAllClass(token)
        );
    }

    /**
     * Returns package location relative path.
     *
     * @param token type token to create implementation for
     * @return a {@link String} representation of package relative path
     */
    private String getFilePath(Class<?> token) {
        return token.getPackageName().replace('.', File.separatorChar);
    }

    /**
     * Creates a <code>.java</code> file containing source code of a class extending or implementing
     * class or interface specified by {@code token} in location specified by {@code root}.
     * 
     * @param token type token to create implementation for
     * @param root root directory.
     * @throws ImplerException if any error occurs during the implementation
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (token == null || root == null) {
            throw new ImplerException("Invalid (null) argument given");
        }
        Path where;
        try {
            where = Path.of(root.toString(), getFilePath(token), getClassName(token) + ".java");
        } catch (InvalidPathException e) {
            throw new ImplerException(e.getMessage());
        }
        ImplementorFileUtils.createDirectoriesTo(where);
        try (BufferedWriter writer = Files.newBufferedWriter(where)) {
            if (token.isPrimitive() || token.isArray() ||
                    Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
                throw new ImplerException("Unsupported class token given");
            }
            try {
                writer.write(encode(getFullClass(token)));
            } catch (IOException e) {
                throw new ImplerException(e.getMessage());
            }
        } catch (IOException e) {
            throw new ImplerException(e.getMessage());
        }
    }

    /**
     * Compiles implemented class extending or implementing {@code token}
     * and stores <code>.class</code> file in given {@code tempDirectory}.
     * Uses <code>-classpath</code> pointing to location of class or interface specified by {@code token}.
     *
     * @param token type token that was implemented
     * @param tempDirectory temporary directory to store temporary <code>.class</code> files in
     * @throws ImplerException if an error occurs during compilation
     */
    private void compileClass(Class<?> token, Path tempDirectory) throws ImplerException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Can not find java compiler");
        }

        Path originPath;
        try {
            String uri = token.getProtectionDomain().getCodeSource().getLocation().getPath();
            if (uri.startsWith("/")) {
                uri = uri.substring(1);
            }
            originPath = Path.of(uri);
        } catch (InvalidPathException e) {
            throw new ImplerException(String.format("Can not find valid class path: %s", e));
        }
        String[] cmdArgs = new String[]{
                "-cp",
                tempDirectory.toString() + File.pathSeparator + originPath.toString(),
                Path.of(tempDirectory.toString(), getFilePath(token), getClassName(token) + ".java").toString()
        };
        if (compiler.run(null, null, null, cmdArgs) != 0) {
            throw new ImplerException("Can not compile generated code");
        }
    }

    /**
     * Builds a <code>.jar</code> file containing compiled by {@link #compileClass(Class, Path)}
     * sources of implemented class using basic {@link Manifest}.
     *
     * @param jarFile path where resulting <code>.jar</code> should be saved
     * @param tempDirectory temporary directory where all <code>.class</code> files are stored
     * @param token type token that was implemented
     * @throws ImplerException if {@link JarOutputStream} processing throws an {@link IOException}
     */
    private void buildJar(Path jarFile, Path tempDirectory, Class<?> token) throws ImplerException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            String pathSuffix = token.getName().replace('.', '/') + "Impl.class";
            stream.putNextEntry(new ZipEntry(pathSuffix));
            Files.copy(Paths.get(tempDirectory.toString(), pathSuffix), stream);
        } catch (IOException e) {
            throw new ImplerException(e.getMessage());
        }
    }

    /**
     * Creates a <code>.jar</code> file containing compiled sources of class
     * implemented by {@link #implement(Class, Path)} class in location specified by {@code jarFile}.
     * Uses temporary directory and deletes it after implementation using {@link ImplementorFileUtils}.
     *
     * @param token type token to create implementation for
     * @param jarFile target <code>.jar</code> file
     * @throws ImplerException if any error occurs during the implementation
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (token == null || jarFile == null) {
            throw new ImplerException("Invalid (null) argument given");
        }
        ImplementorFileUtils.createDirectoriesTo(jarFile.normalize());
        ImplementorFileUtils utils = new ImplementorFileUtils(jarFile.toAbsolutePath().getParent());
        try {
            implement(token, utils.getTempDirectory());
            compileClass(token, utils.getTempDirectory());
            buildJar(jarFile, utils.getTempDirectory(), token);
        } finally {
            utils.cleanTempDirectory();
        }
    }

}
