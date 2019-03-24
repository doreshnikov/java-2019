/**
 * Implementation of {@link info.kgeorgiy.java.advanced.implementor.Impler}
 * and {@link info.kgeorgiy.java.advanced.implementor.JarImpler} interfaces.
 *
 * @author doreshnikov
 * @version 1.0
 */

module ru.ifmo.rain.oreshnikov.implementor {
    requires info.kgeorgiy.java.advanced.implementor;
    requires java.compiler;

    opens ru.ifmo.rain.oreshnikov.implementor;
    exports ru.ifmo.rain.oreshnikov.implementor;
}