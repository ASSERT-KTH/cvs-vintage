@echo off
rem $Id: tomcat.bat,v 1.9 2000/01/27 01:24:51 costin Exp $
rem A batch file to start/stop tomcat server.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set jsdkJars=.\lib\webserver.jar;.\lib\servlet.jar
set jspJars=.\lib\jasper.jar
set beanJars=.\webpages\WEB-INF\classes\jsp\beans
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
if "%1" == "start" goto startServer
if "%1" == "stop" goto stopServer
if "%1" == "run" goto runServer
if "%1" == "env" goto setupEnv

echo Usage:
echo "tomcat (start|run|env|stop)"
echo "        start - start tomcat in a separate window"
echo "        run   - start tomcat in the current window"
echo "        env   - setup the environment for tomcat"
echo "        stop  - stop tomcat"
goto cleanup

:startServer
echo Starting tomcat in new window
echo Using classpath: %CLASSPATH%
rem start java org.apache.tomcat.shell.Startup %2 %3 %4 %5 %6 %7 %8 %9
start java org.apache.tomcat.startup.Tomcat %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runServer
rem Start the Tomcat Server
echo Using classpath: %CLASSPATH%
rem java org.apache.tomcat.shell.Startup %2 %3 %4 %5 %6 %7 %8 %9
java org.apache.tomcat.startup.Tomcat %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:stopServer
rem Stop the Tomcat Server
echo Using classpath: %CLASSPATH%
rem java org.apache.tomcat.shell.Shutdown %2 %3 %4 %5 %6 %7 %8 %9
java org.apache.tomcat.startup.Tomcat -stop %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup
goto cleanup

:setupEnv
set cp=%CLASSPATH%

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
