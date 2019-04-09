@ECHO off

SET pac_name=ru.ifmo.rain.oreshnikov.concurrent
SET pac_path=ru\ifmo\rain\oreshnikov\concurrent
SET res_name=info.kgeorgiy.java.advanced.concurrent
SET res_path=%wd%\modules\%res_name%\info\kgeorgiy\java\advanced\concurrent

SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET out=%wd%\out\production\java-2019
SET req=%wd%\lib;%wd%\artifacts

SET mode=%1

@ECHO on

javac --module-path %req% %wd%\my.sources\%pac_path%\BasicIterativeParallelism.java %res_path%\ScalarIP.java %res_path%\ListIP.java -d %out%
java --class-path %out% --module-path %req% -m %res_name% %mode% %pac_name%.BasicIterativeParallelism %2