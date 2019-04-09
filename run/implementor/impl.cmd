@ECHO off

SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET run=%wd%\run\implementor

SET req=%run%;%wd%\lib;%wd%\artifacts

@ECHO on

java --module-path %req% --add-modules ru.ifmo.rain.oreshnikov.implementor -m info.kgeorgiy.java.advanced.implementor %1 ru.ifmo.rain.oreshnikov.implementor.Implementor %2