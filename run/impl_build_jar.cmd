SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET out=out\production\java-2019
SET module=ru.ifmo.rain.oreshnikov.implementor
SET location=ru\ifmo\rain\oreshnikov\implementor

move %wd%\src\module-info.java module-info.tmp
move module-info.dump %wd%\src\module-info.java

cd %wd%\src
javac -p ..\lib;..\artifacts module-info.java %location%\*.java -d %out%
cd ..\%out%
jar -c --file=%wd%\run\Implementor.jar --main-class=%module%.Implementor --module-path=%wd%\lib;%wd%\artifacts module-info.class %location%\*.class
cd %wd%\run

move %wd%\src\module-info.java module-info.dump
move module-info.tmp %wd%\src\module-info.java