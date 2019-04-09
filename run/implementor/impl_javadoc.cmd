@ECHO off

SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019

SET mod_name=ru.ifmo.rain.oreshnikov.implementor
SET mod_path=ru\ifmo\rain\oreshnikov\implementor
SET res_name=info.kgeorgiy.java.advanced
SET res_path=%wd%\modules\%res_name%.implementor\info\kgeorgiy\java\advanced\implementor

SET out=%wd%\out\production\java-2019
SET src=%wd%\my.modules\%mod_name%
SET run=%wd%\run\implementor

SET req=%wd%\lib;%wd%\artifacts;%run%
IF "%1"=="-m" (
    SET "reference=--module %res_name%.base --module %res_name%.implementor"
) ELSE (
    SET "reference=%res_path%\Impler.java %res_path%\JarImpler.java %res_path%\ImplerException.java"
)

@ECHO on

javadoc -d javadoc -link https://docs.oracle.com/en/java/javase/11/docs/api/ --module-path %req% -private -version -author --module-source-path %wd%\modules;%wd%\my.modules --module %mod_name% %reference%
chrome --new-window %run%\javadoc\index.html