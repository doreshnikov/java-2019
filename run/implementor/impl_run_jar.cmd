@ECHO off

SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET run=%wd%\run\implementor

SET req=%run%;%wd%\lib;%wd%\artifacts;

@ECHO on

java --module-path %req% -m ru.ifmo.rain.oreshnikov.implementor %1 %2 %3