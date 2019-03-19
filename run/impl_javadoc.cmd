SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET module=ru.ifmo.rain.oreshnikov.implementor
SET requirements=info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor
SET location=ru\ifmo\rain\oreshnikov\implementor

move %wd%\src\module-info.java module-info.tmp
move module-info.dump %wd%\src\module-info.java

javadoc -d javadoc -link https://docs.oracle.com/en/java/javase/11/docs/api/ -p ..\lib;..\modules;..\src -private -version -author --module-source-path ..\modules;..\src --module %module% ..\modules\%requirements%\Impler.java ..\modules\%requirements%\JarImpler.java ..\modules\%requirements%\ImplerException.java

move %wd%\src\module-info.java module-info.dump
move module-info.tmp %wd%\src\module-info.java