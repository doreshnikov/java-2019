@ECHO off

SET mod_name=ru.ifmo.rain.oreshnikov.implementor
SET mod_path=ru\ifmo\rain\oreshnikov\implementor

SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET out=%wd%\out\production\java-2019
SET req=%wd%\lib;%wd%\artifacts

SET src=%wd%\my.modules\%mod_name%
SET run=%wd%\run\implementor

@ECHO on

javac --module-path %req% %src%\module-info.java %src%\%mod_path%\*.java -d %out%
cd %out%
jar -c --file=%run%\Implementor.jar --main-class=%mod_name%.Implementor --module-path=%req% module-info.class %mod_path%\*.class
cd %run%