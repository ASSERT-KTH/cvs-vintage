@echo off
rem $Id: tomcat.bat,v 1.6 2000/01/12 13:55:03 rubys Exp $
rem A batch file to start/stop tomcat server.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set spawnCommand=start

set jsdkJars=.\webserver.jar;.\lib\servlet.jar
set jspJars=.\lib\jasper.jar
set beanJars=.\webpages\WEB-INF\classes\jsp\beans;
set miscJars=.\lib\xml.jar
set appJars=%jsdkJars%;%jspJars%;%beanJars%;%miscJars%
set sysJars=%JAVA_HOME%\lib\tools.jar

set appClassPath=.\classes;%appJars%
set cp=%CLASSPATH%

set CLASSPATH=%appClassPath%;%sysJars%

if "%cp%" == "" goto next

rem else
set CLASSPATH=%CLASSPATH%;%cp%

:next
if not "%1" == "-nospawn" goto noSpawnControl
set spawnCommand=
shift

:noSpawnControl
if "%1" == "start" goto startServer
if "%1" == "stop" goto stopServer
echo Usage:
echo "tomcat [-nospawn] (start|stop)"
goto cleanup

:startServer
rem Start the Tomcat Server
echo Using classpath: %CLASSPATH%
%spawnCommand% java org.apache.tomcat.shell.Startup %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:stopServer
rem Stop the Tomcat Server
echo Using classpath: %CLASSPATH%
java org.apache.tomcat.shell.Shutdown %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:cleanup
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
