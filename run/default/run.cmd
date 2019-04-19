@ECHO off

rem walk        RecursiveWalk               RecursiveWalk
rem arrayset    NavigableSet                ArraySet
rem student     AdvancedStudentGroupQuery   StudentDB
rem crawler     hard                        WebCrawler

SET pac_name=ru.ifmo.rain.oreshnikov
SET pac_path=ru\ifmo\rain\oreshnikov

SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET out=%wd%\out\production\java-2019

SET req=%wd%\lib\*;%wd%\artifacts\*
SET mreq=%wd%\lib;%wd%\artifacts

SET task=%1
SET mypac=%task%
IF "%mypac%"=="mapper" (
    SET mypac=concurrent
)

SET dir=%wd%\my.sources\%pac_path%\%mypac%
SET cls=%pac_name%.%mypac%

IF "%task%"=="walk" (
    SET mode=RecursiveWalk
    SET class=%cls%.RecursiveWalk
) ELSE IF "%task%"=="arrayset" (
    SET mode=NavigableSet
    SET class=%cls%.ArraySet
) ELSE IF "%task%"=="student" (
    SET mode=AdvancedStudentGroupQuery
    SET class=%cls%.StudentDB
) ELSE IF "%task%"=="mapper" (
    SET mode=list
    SET class=%cls%.ParallelMapperImpl,%cls%.IterativeParallelism
) ELSE IF "%task%"=="crawler" (
    SET mode=hard
    SET class=%cls%.WebCrawler
) ELSE (
    ECHO invalid task name %task%
)

@ECHO on

javac --class-path %req% %dir%\*.java -d %out%
java --class-path %out% --module-path %mreq% -m info.kgeorgiy.java.advanced.%task% %mode% %class% %2