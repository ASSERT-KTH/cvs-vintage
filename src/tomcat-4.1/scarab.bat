@echo off
if "%1"=="-h" goto usage
if "%1"=="-l" goto usedefaultmem

goto setmem

:usage
    echo Usage: scarab.sh [-l] [-h]
    echo -l    Default JVM memory allocation
    echo -h    This usage information
    goto done

:setmem
    set CATALINA_OPTS=-Xms96M -Xmx256M
    goto startcatalina

:usedefaultmem
    goto startcatalina

:startcatalina
    if not "-%CLASSPATH%-"=="--" echo Classpath: %CLASSPATH%
    .\bin\catalina.bat run

:done