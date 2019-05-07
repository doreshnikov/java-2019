@ECHO off

SET pac_name=ru.ifmo.rain.oreshnikov
SET pac_path=ru\ifmo\rain\oreshnikov

SET wd=C:\Users\isuca\projects\itmo\4-semester\java-2019
SET out=%wd%\out\production\java-2019
SET run=%wd%\run\default

SET req=%wd%\lib\*;%wd%\artifacts\*
SET mreq=%wd%\lib;%wd%\artifacts

SET task=%1
SET mypac=%task%
SET kgpac=%task%
IF "%mypac%"=="mapper" (
    SET mypac=concurrent
) ELSE IF "%mypac%"=="helloserver" (
    SET mypac=hello
    SET kgpac=hello
) ELSE IF "%mypac%"=="helloclient" (
    SET mypac=hello
    SET kgpac=hello
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
) ELSE IF "%task%"=="helloclient" (
    SET mode=client-i18n
    SET class=%cls%.HelloUDPClient
) ELSE IF "%task%"=="helloserver" (
    SET mode=server-i18n
    SET class=%cls%.HelloUDPServer
) ELSE IF "%task%"=="hello" (
    run helloclient %2
    run helloserver %2
    GOTO end
) ELSE (
    ECHO invalid task name %task%
    GOTO end
)

@ECHO on

javac --class-path %req% %dir%\*.java -d %out%
java --class-path %out% --module-path %mreq% -m info.kgeorgiy.java.advanced.%kgpac% %mode% %class% %2

:end