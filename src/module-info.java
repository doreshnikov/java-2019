/**
 * @author doreshnikov
 * @date 12-Mar-19
 */

module ru.ifmo.rain.oreshnikov.implementor {
//    requires info.kgeorgiy.java.advanced.implementor;
    requires java.compiler;
    requires java.management;
    requires java.management.rmi;
    requires java.desktop;
    requires java.naming;
    requires java.sql;
    requires java.sql.rowset;

    requires junit;
    requires quickcheck;

    opens ru.ifmo.rain.oreshnikov.implementor;
    exports ru.ifmo.rain.oreshnikov.implementor;
}