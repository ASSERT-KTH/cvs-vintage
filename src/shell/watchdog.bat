@echo off

SET WATCHDOG_HOME=..\watchdog

set TOM_PREV_CLASSPATH=%CLASSPATH%

SET CLASSPATH=lib\ant.jar;%CLASSPATH%
CALL bin\tomcat env
SET CLASSPATH=%WATCHDOG_HOME%\lib\moo.jar;%CLASSPATH%
SET CLASSPATH=%WATCHDOG_HOME%\lib\client.jar;%CLASSPATH%


echo "using classpath=" %CLASSPATH%
if "%1"=="servlet" goto servlet

java org.apache.tools.ant.Main -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/jsp.xml jsp-test

if "%1"=="jsp" goto restore

:servlet
java org.apache.tools.ant.Main -Dwatchdog.home %WATCHDOG_HOME% -f %WATCHDOG_HOME%/conf/servlet.xml servlet-test

:restore
set CLASSPATH=%TOM_PREV_CLASSPATH%

