@ECHO off

rem walk       RecursiveWalk             RecursiveWalk
rem arrayset   NavigableSet              ArraySet

SET pac_name=ru.ifmo.rain.oreshnikov
SET pac_path=ru\ifmo\rain\oreshnikov

SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET out=%wd%\out\production\java-2019
SET req=%wd%\lib;%wd%\artifacts

SET task=%1
SET mode=%2

@ECHO on

javac --module-path %req% %wd%\my.sources\%pac_path%\%task%\*.java %5 %6 %7 %8 %9 -d %out%
java --class-path %out% --module-path %req% -m info.kgeorgiy.java.advanced.%task% %mode% %pac_name%.%task%.%3 %4