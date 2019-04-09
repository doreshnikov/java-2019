@ECHO off

SET pac_name=ru.ifmo.rain.oreshnikov.concurrent
SET pac_path=ru\ifmo\rain\oreshnikov\concurrent

SET res_name=info.kgeorgiy.java.advanced
SET res_path=info\kgeorgiy\java\advanced

SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET out=%wd%\out\production\java-2019
SET req=%wd%\lib;%wd%\artifacts

SET con_path=%wd%\modules\%res_name%.concurrent\%res_path%\concurrent
SET map_path=%wd%\modules\%res_name%.mapper\%res_path%\mapper

SET mode=%1

@ECHO on

javac --module-path %req% %wd%\my.sources\%pac_path%\*.java %map_path%\ParallelMapper.java %con_path%\ScalarIP.java %con_path%\ListIP.java -d %out%
java --class-path %out% --module-path %req% -m %res_name%.mapper %mode% %pac_name%.ParallelMapperImpl,%pac_name%.IterativeParallelism %2