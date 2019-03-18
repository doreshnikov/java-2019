/**
 * @author doreshnikov
 * @date 12-Mar-19
 */

module ru.ifmo.rain.oreshnikov.implementor {
    requires info.kgeorgiy.java.advanced.implementor;
    requires java.compiler;

    opens ru.ifmo.rain.oreshnikov.implementor to info.kgeorgiy.java.advanced.implementor;
    exports ru.ifmo.rain.oreshnikov.implementor;
}