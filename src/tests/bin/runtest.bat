@echo off
rem $Id: runtest.bat,v 1.2 1999/10/15 22:16:50 costin Exp $
rem Startup batch file for servlet runner.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set host=localhost
set port=8080
set test=testlist.txt

set cp=%CLASSPATH%

set CLASSPATH=classes;lib\moo.jar

set TOMCAT_HOME=..
%TOMCAT_HOME%\tomcatEnv.bat

if "%cp%" == "" goto next

rem else
set CLASSPATH=%CLASSPATH%;%cp%

:next
echo Using classpath: %CLASSPATH%

start java org.apache.tomcat.shell.Startup %1 %2 %3 %4 %5 %6 %7 %8 %9
rem java org.apache.tomcat.shell.Startup %1 %2 %3 %4 %5 %6 %7 %8 %9
sleep 5
java -Dtest.hostName=%host% -Dtest.port=%port% org.apache.tools.moo.Main \
    -testfile %test%
java org.apache.tomcat.shell.Shutdown %1 %2 %3 %4 %5 %6 %7 %8 %9

rem clean up

set CLASSPATH=%cp%
set port=
set host=
set test=
set cp=

rem pause
