@echo off
rem A batch file to start/stop tomcat server.

rem This batch file written and tested under Windows NT
rem Improvements to this file are welcome

set cp=%CLASSPATH%

set CLASSPATH=.
set CLASSPATH=%TOMCAT_HOME%\classes
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\webserver.jar
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\jasper.jar
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\xml.jar
set CLASSPATH=%CLASSPATH%;%TOMCAT_HOME%\lib\servlet.jar
set CLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar

if "%cp%" == "" goto next

rem else
set CLASSPATH=%CLASSPATH%;%cp%

:next
if "%1" == "start" goto startServer
if "%1" == "stop" goto stopServer
if "%1" == "run" goto runServer
if "%1" == "env" goto setupEnv
if "%1" == "ant" goto runAnt
if "%1" == "jspc" goto runJspc

echo Usage:
echo tomcat (start^|run^|env^|stop)
echo         start - start tomcat in a separate window
echo         run   - start tomcat in the current window
echo         env   - setup the environment for tomcat
echo         stop  - stop tomcat
echo         ant   - run ant with tomcat context
echo         jspc  - run jsp pre compiler
goto cleanup

:startServer
echo Starting tomcat in new window
echo Using classpath: %CLASSPATH%
start java -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runServer
rem Start the Tomcat Server
echo Using classpath: %CLASSPATH%
java -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:stopServer
rem Stop the Tomcat Server
echo Using classpath: %CLASSPATH%
java -Dtomcat.home="%TOMCAT_HOME%" org.apache.tomcat.startup.Tomcat -stop %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup
goto cleanup

:runAnt
rem Run ant
echo Using classpath: %CLASSPATH%
java -Dant.home="%TOMCAT_HOME%" -Dtomcat.home="%TOMCAT_HOME%" org.apache.tools.ant.Main %2 %3 %4 %5 %6 %7 %8 %9
goto cleanup

:runJspc
rem Run ant
echo Using classpath: %CLASSPATH%
java -Dtomcat.home="%TOMCAT_HOME%" org.apache.jasper.JspC %2 %3 %4 %5 %6 %7 %8 %9
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
