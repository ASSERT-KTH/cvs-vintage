@echo off
rem $Id: runtest.bat,v 1.2 1999/10/14 23:50:34 akv Exp $
rem Startup batch file for servlet runner.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set host=localhost
set port=8080
set test=testlist.txt

set topDir=..\..\..
set tomcatBuildDir=%topDir%\build\tomcat
set toolsDir=%topDir%\jakarta-tools

set jsdkJars=%tomcatBuildDir%\webserver.jar;%tomcatBuildDir%\lib\servlet.jar
set jspJars=%tomcatBuildDir%\lib\jasper.jar
set beanJars=
set miscJars=%toolsDir%\projectx-tr2.jar;%toolsDir%\moo.jar
set appJars=%jsdkJars%;%jspJars%;%miscJars%
set sysJars=%JAVA_HOME%\lib\tools.jar

set appClassPath=.\classes;%appJars%
set cp=%CLASSPATH%

set CLASSPATH=%appClassPath%;%sysJars%

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
set jsdkJars=
set jspJars=
set beanJars=
set miscJars=
set appJars=
set appClassPath=
set cp=

rem pause
