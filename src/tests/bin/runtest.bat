@echo off
rem $Id: runtest.bat,v 1.3 1999/10/15 22:55:48 mode Exp $
rem Startup batch file for servlet runner.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set host=localhost
set port=8080
set test=testlist.txt
set topDir=..\..\..
set toolsDir=%topDir%\jakarta-tools
set miscJars=%toolsDir%\projectx-tr2.jar
set sysJars=%JAVA_HOME%\lib\tools.jar

set cp=%CLASSPATH%

set CLASSPATH=classes;lib\moo.jar;%miscJars%;%sysJars%

set TOMCAT_HOME=..
set TOM_CLASSPATH=%TOMCAT_HOME%\webserver.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\servlet.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\jasper.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\xml.jar;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\webpages\WEB-INF\classes\jsp\beans;%TOM_CLASSPATH%
set TOM_CLASSPATH=%TOMCAT_HOME%\classes;%TOM_CLASSPATH%

set TOM_CLASSPATH=%JAVA_HOME%\lib\tools.jar;%TOM_CLASSPATH%


set TOM_PREV_CLASSPATH=%CLASSPATH%
set CLASSPATH=%TOM_CLASSPATH%;%CLASSPATH%

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
