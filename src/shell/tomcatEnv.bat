@echo off

if "%1" == "restore" goto restore

set TOM_PREV_CLASSPATH=%CLASSPATH%
set TOM_PREV_HOME=%TOMCAT_HOME%

if not "%TOMCAT_HOME%" == "" goto start

SET TOMCAT_HOME=.
if exist "%TOMCAT_HOME%\bin\tomcat.bat" goto start

SET TOMCAT_HOME=..
if exist "%TOMCAT_HOME%\bin\tomcat.bat" goto start

SET TOMCAT_HOME=
echo Unable to determine the value of TOMCAT_HOME.
goto eof

:start
call "%TOMCAT_HOME%\bin\tomcat" env %*
goto eof

:restore
set CLASSPATH=%TOM_PREV_CLASSPATH%
set TOMCAT_HOME=%TOM_PREV_HOME%
set TOM_PREV_CLASSPATH=
set TOM_PREV_HOME=

:eof
