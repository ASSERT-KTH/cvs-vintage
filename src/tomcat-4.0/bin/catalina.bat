@echo off
rem ---------------------------------------------------------------------------
rem catalina.bat - Start/Stop Script for the CATALINA Server
rem
rem Environment Variable Prequisites:
rem
rem   CATALINA_HOME (Optional) May point at your Catalina "build" directory.
rem                 If not present, the current working directory is assumed.
rem
rem   CATALINA_OPTS (Optional) Java runtime options used when the "start",
rem                 "stop", or "run" command is executed.
rem
rem   JAVA_HOME     Must point at your Java Development Kit installation.
rem
rem $Id: catalina.bat,v 1.9 2001/08/13 05:01:49 jon Exp $
rem ---------------------------------------------------------------------------


rem ----- Save Environment Variables That May Change --------------------------

set _CATALINA_HOME=%CATALINA_HOME%
set _CLASSPATH=%CLASSPATH%
set _CP=%CP%


rem ----- Verify and Set Required Environment Variables -----------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

if not "%CATALINA_HOME%" == "" goto gotCatalinaHome
set CATALINA_HOME=.
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okCatalinaHome
set CATALINA_HOME=..
:gotCatalinaHome
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okCatalinaHome
echo Cannot find catalina.bat in %CATALINA_HOME%\bin
echo Please check your CATALINA_HOME setting
goto cleanup
:okCatalinaHome


rem ----- Prepare Appropriate Java Execution Commands -------------------------

if not "%OS%" == "Windows_NT" goto noTitle
set _STARTJAVA=start "Catalina" "%JAVA_HOME%\bin\java"
set _RUNJAVA="%JAVA_HOME%\bin\java"
goto gotTitle
:noTitle
set _STARTJAVA=start "%JAVA_HOME%\bin\java"
set _RUNJAVA="%JAVA_HOME%\bin\java"
:gotTitle

rem ----- Set Up The Runtime Classpath ----------------------------------------

set CP=%CATALINA_HOME%\bin\bootstrap.jar;%JAVA_HOME%\lib\tools.jar
set CLASSPATH=%CP%
echo Using CLASSPATH: %CLASSPATH%


rem ----- Execute The Requested Command ---------------------------------------

if "%1" == "env" goto doEnv
if "%1" == "run" goto doRun
if "%1" == "start" goto doStart
if "%1" == "stop" goto doStop

:doUsage
echo Usage:  catalina ( env ^| run ^| start ^| stop )
echo Commands:
echo   env -   Set up environment variables that Catalina would use
echo   run -   Start Catalina in the current window
echo   start - Start Catalina in a separate window
echo   stop -  Stop Catalina
goto cleanup

:doEnv
goto finish

:doRun
if "%2" == "-security" goto doRunSecure
%_RUNJAVA% %CATALINA_OPTS% -Dcatalina.home="%CATALINA_HOME%" org.apache.catalina.startup.Bootstrap %2 %3 %4 %5 %6 %7 %8 %9 start
goto cleanup
:doRunSecure
%_RUNJAVA% %CATALINA_OPTS% -Djava.security.manager -Djava.security.policy=="%CATALINA_HOME%/conf/catalina.policy" -Dcatalina.home="%CATALINA_HOME%" org.apache.catalina.startup.Bootstrap %3 %4 %5 %6 %7 %8 %9 start
goto cleanup

:doStart
if "%2" == "-security" goto doStartSecure
%_STARTJAVA% %CATALINA_OPTS% -Dcatalina.home="%CATALINA_HOME%" org.apache.catalina.startup.Bootstrap %2 %3 %4 %5 %6 %7 %8 %9 start
goto cleanup
:doStartSecure
echo Using Security Manager
%_STARTJAVA% %CATALINA_OPTS% -Djava.security.manager -Djava.security.policy=="%CATALINA_HOME%/conf/catalina.policy" -Dcatalina.home="%CATALINA_HOME%" org.apache.catalina.startup.Bootstrap %3 %4 %5 %6 %7 %8 %9 start
goto cleanup

:doStop
%_RUNJAVA% %CATALINA_OPTS% -Dcatalina.home="%CATALINA_HOME%" org.apache.catalina.startup.Bootstrap %2 %3 %4 %5 %6 %7 %8 %9 stop
goto cleanup



rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set CATALINA_HOME=%_CATALINA_HOME%
set _CATALINA_HOME=
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set CP=%_CP%
set _LIBJARS=
set _RUNJAVA=
set _STARTJAVA=
:finish
