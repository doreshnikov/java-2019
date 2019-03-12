SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET out=out\production\java-2019
SET me-mod=ru.ifmo.rain.oreshnikov.implementor
SET me-loc=ru\ifmo\rain\oreshnikov\implementor
SET kg-mod=info.kgeorgiy.java.advanced.implementor
SET kg-loc=info\kgeorgiy\java\advanced\implementor

cd %wd%\%out%
jar xf %wd%\artifacts\%kg-mod%.jar %kg-loc%\Impler.class %kg-loc%\JarImpler.class %kg-loc%\ImplerException.class
jar -c --file=%wd%\run\Implementor.jar --main-class=%me-mod%.Implementor module-info.class %me-loc%\*.class %kg-loc%\*.class

rmdir info /S /Q
cd %wd%\run