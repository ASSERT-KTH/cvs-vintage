@echo off

if "%1" == "restore" goto restore

set TOM_PREV_CLASSPATH=%CLASSPATH%

if "%TOMCAT_HOME%" == "" goto bin
call %TOMCAT_HOME%\bin\tomcat env
goto :eof

:bin
CALL bin\tomcat env
goto eof

:restore
set CLASSPATH=%TOM_PREV_CLASSPATH%

:eof
