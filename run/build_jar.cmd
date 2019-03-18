SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET out=out\production\java-2019
SET me-mod=ru.ifmo.rain.oreshnikov.implementor
SET me-loc=ru\ifmo\rain\oreshnikov\implementor

cd %wd%\%out%
jar -c --file=%wd%\run\Implementor.jar --main-class=%me-mod%.Implementor --module-path=%wd%\lib;%wd%\artifacts module-info.class %me-loc%\*.class
cd %wd%\run