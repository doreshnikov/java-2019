SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET out=out\production\java-2019
SET me-mod=ru.ifmo.rain.oreshnikov.implementor
SET me-loc=ru\ifmo\rain\oreshnikov\implementor

cd %wd%
javac -d %out% -p lib;artifacts; src\%me-mod%\module-info.java src\%me-mod%\%me-loc%\*.java

cd %wd%\run